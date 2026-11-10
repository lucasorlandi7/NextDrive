package br.edu.unoesc.nextdrive.service;

import br.edu.unoesc.nextdrive.dao.ClienteDAO;
import br.edu.unoesc.nextdrive.dao.LocacaoDAO;
import br.edu.unoesc.nextdrive.dao.MultaDAO;
import br.edu.unoesc.nextdrive.dao.PagamentoDAO;
import br.edu.unoesc.nextdrive.dao.ReservaDAO;
import br.edu.unoesc.nextdrive.dao.VeiculoDAO;
import br.edu.unoesc.nextdrive.model.ClientePessoaFisica;
import br.edu.unoesc.nextdrive.model.Locacao;
import br.edu.unoesc.nextdrive.model.Multa;
import br.edu.unoesc.nextdrive.model.Pagamento;
import br.edu.unoesc.nextdrive.model.Reserva;
import br.edu.unoesc.nextdrive.model.StatusLocacao;
import br.edu.unoesc.nextdrive.model.StatusReserva;
import br.edu.unoesc.nextdrive.model.Usuario;
import br.edu.unoesc.nextdrive.model.Veiculo;
import br.edu.unoesc.nextdrive.util.Tabela;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Retirada, devolução, pagamento e multas (RF09 a RF11, RF15, RF16, RF24). */
public class LocacaoService extends Servico {
    private final Usuario funcionario;

    /**
     * @param funcionario atendente ou gerente que opera o balcão
     */
    public LocacaoService(Usuario funcionario) {
        super(papelDe(funcionario));
        this.funcionario = funcionario;
    }

    /**
     * Veículos que podem ser entregues agora para a reserva.
     *
     * @param reservaId código da reserva ativa
     * @return veículos da categoria, livres na loja de retirada
     * @throws NegocioException se a reserva não existir ou não estiver ativa
     */
    public List<Veiculo> veiculosParaRetirada(int reservaId) throws NegocioException {
        return executar(con -> {
            Reserva r = reservaAtiva(con, reservaId);
            return new VeiculoDAO(con).disponiveis(r.getCategoriaId(), r.getLojaRetiradaId(),
                    LocalDateTime.now(), r.getDataDevolucaoPrevista(), true);
        });
    }

    /**
     * @param reservaId código da reserva
     * @return a pessoa física que vai dirigir (cliente ou condutor responsável da empresa)
     * @throws NegocioException se a reserva não existir
     */
    public ClientePessoaFisica condutorDaReserva(int reservaId) throws NegocioException {
        return executar(con -> new ClienteDAO(con).condutorDaReserva(reservaId)
                .orElseThrow(() -> new NegocioException("Reserva ou condutor não encontrado.")));
    }

    /**
     * Registra a retirada. Antes de chamar o banco, confere idade e CNH do condutor para dar uma
     * mensagem clara; a procedure e os triggers repetem a validação (RN05, RF16).
     *
     * @param reservaId  reserva ativa
     * @param veiculoId  veículo escolhido
     * @param kmSaida    quilometragem de saída
     * @param combustivel nível de combustível de saída, de 0 a 100 (%)
     * @throws NegocioException se a retirada não for permitida
     */
    public void abrir(int reservaId, int veiculoId, int kmSaida, int combustivel) throws NegocioException {
        verificarFaixa(combustivel);
        if (kmSaida < 0) throw new NegocioException("A quilometragem não pode ser negativa.");
        executarSemRetorno(con -> {
            ClientePessoaFisica condutor = new ClienteDAO(con).condutorDaReserva(reservaId)
                    .orElseThrow(() -> new NegocioException("Reserva ou condutor não encontrado."));
            String problema = condutor.validarCondutor(LocalDate.now());
            if (problema != null) throw new NegocioException("Retirada não permitida: " + problema);
            new LocacaoDAO(con).abrir(reservaId, veiculoId, condutor.getId(), funcionario.getId(), kmSaida, combustivel);
            return null;
        });
    }

    /**
     * Registra a devolução; o banco calcula e grava o valor final.
     *
     * @param locacaoId   locação aberta
     * @param kmChegada   quilometragem na devolução
     * @param combustivel nível de combustível na devolução, de 0 a 100 (%)
     * @param lojaId      loja onde o veículo foi devolvido
     * @param desconto    desconto em reais (pode ser vazio)
     * @return a locação encerrada, com os valores calculados
     * @throws NegocioException se a devolução não for permitida
     */
    public Locacao devolver(int locacaoId, int kmChegada, int combustivel, int lojaId, BigDecimal desconto)
            throws NegocioException {
        verificarFaixa(combustivel);
        if (desconto != null && desconto.signum() < 0) throw new NegocioException("O desconto não pode ser negativo.");
        return executar(con -> {
            LocacaoDAO dao = new LocacaoDAO(con);
            dao.devolver(locacaoId, kmChegada, combustivel, lojaId, funcionario.getId(), desconto);
            return dao.buscar(locacaoId).orElseThrow(() -> new NegocioException("Locação não encontrada."));
        });
    }

    /**
     * Registra um pagamento. Se o total pago atingir o valor final, a locação passa a PAGA e a
     * cautela é liberada quando não há multas pendentes.
     *
     * @param p pagamento (cartão ou PIX)
     * @return a locação atualizada
     * @throws NegocioException se o pagamento for inválido ou maior que o saldo
     */
    public Locacao pagar(Pagamento p) throws NegocioException {
        try {
            p.validar();
        } catch (IllegalArgumentException e) {
            throw new NegocioException(e.getMessage());
        }
        return executar(con -> {
            LocacaoDAO dao = new LocacaoDAO(con);
            Locacao l = dao.buscar(p.getLocacaoId()).orElseThrow(() -> new NegocioException("Locação não encontrada."));
            if (l.getStatus() == StatusLocacao.ABERTA) throw new NegocioException("Registre a devolução antes do pagamento.");
            if (l.getStatus() == StatusLocacao.PAGA) throw new NegocioException("Esta locação já está paga.");
            if (p.getValor().compareTo(l.saldoDevedor()) > 0)
                throw new NegocioException("O valor é maior que o saldo devedor (R$ " + l.saldoDevedor() + ").");
            new PagamentoDAO(con).registrar(p, funcionario.getId());
            return dao.buscar(p.getLocacaoId()).orElseThrow(() -> new NegocioException("Locação não encontrada."));
        });
    }

    /**
     * Registra uma multa de trânsito recebida depois da devolução.
     *
     * @param m multa preenchida
     * @throws NegocioException se os dados forem inválidos
     */
    public void registrarMulta(Multa m) throws NegocioException {
        try {
            m.validar();
        } catch (IllegalArgumentException e) {
            throw new NegocioException(e.getMessage());
        }
        executarSemRetorno(con -> { new MultaDAO(con).inserir(m, funcionario.getId()); return null; });
    }

    /**
     * Marca uma multa como paga; ao pagar a última pendente, o banco libera a cautela.
     *
     * @param multaId código da multa
     * @throws NegocioException em erro de banco
     */
    public void pagarMulta(int multaId) throws NegocioException {
        executarSemRetorno(con -> { new MultaDAO(con).pagar(multaId); return null; });
    }

    /** @return multas pendentes @throws NegocioException em erro de banco */
    public Tabela multasPendentes() throws NegocioException { return executar(con -> new MultaDAO(con).pendentes()); }

    /** @return locações abertas @throws NegocioException em erro de banco */
    public Tabela abertas() throws NegocioException { return executar(con -> new LocacaoDAO(con).abertas()); }

    /** @return locações encerradas com saldo @throws NegocioException em erro de banco */
    public Tabela aguardandoPagamento() throws NegocioException {
        return executar(con -> new LocacaoDAO(con).aguardandoPagamento());
    }

    /**
     * @param locacaoId código da locação
     * @return resumo da locação (uma linha), vazio se não existir
     * @throws NegocioException em erro de banco
     */
    public Tabela detalhe(int locacaoId) throws NegocioException {
        return executar(con -> new LocacaoDAO(con).detalhe(locacaoId));
    }

    /**
     * @param locacaoId código da locação
     * @return a locação completa
     * @throws NegocioException se não existir
     */
    public Locacao buscar(int locacaoId) throws NegocioException {
        return executar(con -> new LocacaoDAO(con).buscar(locacaoId)
                .orElseThrow(() -> new NegocioException("Locação não encontrada.")));
    }

    /**
     * @param locacaoId código da locação
     * @return pagamentos já feitos
     * @throws NegocioException em erro de banco
     */
    public Tabela pagamentos(int locacaoId) throws NegocioException {
        return executar(con -> new PagamentoDAO(con).porLocacao(locacaoId));
    }

    private Reserva reservaAtiva(java.sql.Connection con, int reservaId) throws java.sql.SQLException, NegocioException {
        Reserva r = new ReservaDAO(con).buscar(reservaId)
                .orElseThrow(() -> new NegocioException("Reserva não encontrada."));
        if (r.getStatus() != StatusReserva.ATIVA) throw new NegocioException("A reserva não está ativa (" + r.getStatus() + ").");
        return r;
    }

    private void verificarFaixa(int combustivel) throws NegocioException {
        if (combustivel < 0 || combustivel > 100) throw new NegocioException("O combustível deve ficar entre 0 e 100%.");
    }
}
