package br.edu.unoesc.nextdrive.service;

import br.edu.unoesc.nextdrive.dao.CategoriaDAO;
import br.edu.unoesc.nextdrive.dao.LojaDAO;
import br.edu.unoesc.nextdrive.dao.ReservaDAO;
import br.edu.unoesc.nextdrive.dao.VeiculoDAO;
import br.edu.unoesc.nextdrive.model.CategoriaVeiculo;
import br.edu.unoesc.nextdrive.model.Loja;
import br.edu.unoesc.nextdrive.model.Reserva;
import br.edu.unoesc.nextdrive.model.Usuario;
import br.edu.unoesc.nextdrive.util.Conexao.Papel;
import br.edu.unoesc.nextdrive.util.Tabela;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.List;

/** Disponibilidade e reservas (RF06, RF07, RF08). */
public class ReservaService extends Servico {

    /** Resultado da consulta de disponibilidade. */
    public static final class Disponibilidade {
        private final int livres;
        private final int reservadas;
        private final BigDecimal estimativa;

        Disponibilidade(int livres, int reservadas, BigDecimal estimativa) {
            this.livres = livres;
            this.reservadas = reservadas;
            this.estimativa = estimativa;
        }

        /** @return veículos livres de locação e manutenção no período */
        public int getLivres() { return livres; }
        /** @return reservas ativas que já disputam esses veículos */
        public int getReservadas() { return reservadas; }
        /** @return valor estimado da locação no período */
        public BigDecimal getEstimativa() { return estimativa; }
        /** @return true se ainda há veículo para uma nova reserva */
        public boolean isDisponivel() { return livres > reservadas; }
    }

    /** Serviço sem usuário (consulta pública de lojas e categorias). */
    public ReservaService() { super(Papel.SITE); }

    /** Serviço para um usuário logado. */
    public ReservaService(Usuario u) { super(papelDe(u)); }

    /**
     * @return as lojas ativas
     * @throws NegocioException em erro de banco
     */
    public List<Loja> lojas() throws NegocioException { return executar(con -> new LojaDAO(con).listarAtivas()); }

    /**
     * @return as categorias de veículos
     * @throws NegocioException em erro de banco
     */
    public List<CategoriaVeiculo> categorias() throws NegocioException {
        return executar(con -> new CategoriaDAO(con).listar());
    }

    /**
     * Consulta se há veículo na categoria e loja no período, descontando as reservas já feitas
     * (RN03, RN04, RN11), e calcula a estimativa de valor.
     *
     * @return o resultado da consulta
     * @throws NegocioException se os dados forem inválidos
     */
    public Disponibilidade consultar(int categoriaId, int lojaId, LocalDateTime inicio, LocalDateTime fim)
            throws NegocioException {
        return executar(con -> consultar(con, categoriaId, lojaId, inicio, fim));
    }

    private Disponibilidade consultar(java.sql.Connection con, int categoriaId, int lojaId, LocalDateTime inicio,
                                      LocalDateTime fim) throws java.sql.SQLException, NegocioException {
        if (!fim.isAfter(inicio)) throw new NegocioException("A devolução deve ser depois da retirada.");
        CategoriaVeiculo cat = new CategoriaDAO(con).buscar(categoriaId)
                .orElseThrow(() -> new NegocioException("Categoria não encontrada."));
        int livres = new VeiculoDAO(con).disponiveis(categoriaId, lojaId, inicio, fim, false).size();
        int reservadas = new ReservaDAO(con).contarAtivasConflitantes(categoriaId, lojaId, inicio, fim);
        return new Disponibilidade(livres, reservadas, cat.calcularEstimativa(inicio, fim));
    }

    /**
     * Cria a reserva depois de conferir a disponibilidade. A conferência e a gravação ficam na mesma
     * transação, protegidas por um bloqueio (advisory lock) por categoria e loja, para duas
     * reservas simultâneas não ocuparem o último veículo.
     *
     * @param r     reserva preenchida (cliente, categoria, lojas e período)
     * @param quem  usuário que registra; funcionário = balcão, cliente = site
     * @return o código da reserva
     * @throws NegocioException se os dados forem inválidos ou não houver veículo disponível
     */
    public int criar(Reserva r, Usuario quem) throws NegocioException {
        try {
            r.validar();
        } catch (IllegalArgumentException e) {
            throw new NegocioException(e.getMessage());
        }
        if (quem.isFuncionario()) {
            r.setCanal(br.edu.unoesc.nextdrive.model.Canal.BALCAO);
            r.setFuncionarioId(quem.getId());
        } else {
            r.setCanal(br.edu.unoesc.nextdrive.model.Canal.SITE);
            r.setClienteId(quem.getId());
        }
        return executar(con -> {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement("SELECT pg_advisory_xact_lock(?, ?)")) {
                    ps.setInt(1, r.getCategoriaId());
                    ps.setInt(2, r.getLojaRetiradaId());
                    try (ResultSet rs = ps.executeQuery()) { rs.next(); }
                }
                Disponibilidade d = consultar(con, r.getCategoriaId(), r.getLojaRetiradaId(),
                        r.getDataRetiradaPrevista(), r.getDataDevolucaoPrevista());
                if (!d.isDisponivel())
                    throw new NegocioException("Não há veículo disponível nessa categoria, loja e período.");
                r.setValorEstimado(d.getEstimativa());
                int id = new ReservaDAO(con).inserir(r);
                con.commit();
                return id;
            } catch (java.sql.SQLException | NegocioException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        });
    }

    /**
     * Cancela uma reserva ativa (RF08).
     *
     * @param reservaId     código da reserva
     * @param justificativa motivo obrigatório
     * @throws NegocioException se a justificativa faltar ou a reserva não puder ser cancelada
     */
    public void cancelar(int reservaId, String justificativa) throws NegocioException {
        if (justificativa == null || justificativa.isBlank())
            throw new NegocioException("Informe a justificativa do cancelamento.");
        executarSemRetorno(con -> { new ReservaDAO(con).cancelar(reservaId, justificativa.trim()); return null; });
    }

    /**
     * @return reservas ativas de todos os clientes (para o atendente)
     * @throws NegocioException em erro de banco
     */
    public Tabela ativas() throws NegocioException { return executar(con -> new ReservaDAO(con).ativas()); }

    /**
     * @param clienteId código do cliente
     * @return todas as reservas do cliente
     * @throws NegocioException em erro de banco
     */
    public Tabela doCliente(int clienteId) throws NegocioException {
        return executar(con -> new ReservaDAO(con).porCliente(clienteId));
    }

    /**
     * @return situação da frota por loja e categoria
     * @throws NegocioException em erro de banco
     */
    public Tabela situacaoDaFrota() throws NegocioException { return executar(con -> new VeiculoDAO(con).situacaoDaFrota()); }
}
