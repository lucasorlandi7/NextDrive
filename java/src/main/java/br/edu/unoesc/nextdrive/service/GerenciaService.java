package br.edu.unoesc.nextdrive.service;

import br.edu.unoesc.nextdrive.dao.CategoriaDAO;
import br.edu.unoesc.nextdrive.dao.FuncionarioDAO;
import br.edu.unoesc.nextdrive.dao.LojaDAO;
import br.edu.unoesc.nextdrive.dao.ManutencaoDAO;
import br.edu.unoesc.nextdrive.dao.RelatorioDAO;
import br.edu.unoesc.nextdrive.dao.VeiculoDAO;
import br.edu.unoesc.nextdrive.model.CategoriaVeiculo;
import br.edu.unoesc.nextdrive.model.Funcionario;
import br.edu.unoesc.nextdrive.model.Loja;
import br.edu.unoesc.nextdrive.model.Manutencao;
import br.edu.unoesc.nextdrive.model.StatusVeiculo;
import br.edu.unoesc.nextdrive.model.Usuario;
import br.edu.unoesc.nextdrive.model.Veiculo;
import br.edu.unoesc.nextdrive.util.Conexao.Papel;
import br.edu.unoesc.nextdrive.util.Tabela;
import java.math.BigDecimal;
import java.util.List;

/** Operações exclusivas do gerente: frota, preços, manutenção, funcionários e relatórios. */
public class GerenciaService extends Servico {
    private final Usuario gerente;

    /**
     * @param gerente usuário com perfil de gerente
     * @throws NegocioException se o usuário não for gerente (RN10)
     */
    public GerenciaService(Usuario gerente) throws NegocioException {
        super(Papel.GERENTE);
        if (!gerente.isGerente()) throw new NegocioException("Apenas gerentes podem acessar esta área.");
        this.gerente = gerente;
    }

    /** @return frota com utilização @throws NegocioException em erro de banco */
    public Tabela frota() throws NegocioException { return executar(con -> new VeiculoDAO(con).frota()); }

    /** @return veículos que podem ir para manutenção @throws NegocioException em erro de banco */
    public List<Veiculo> veiculosParaManutencao() throws NegocioException {
        return executar(con -> new VeiculoDAO(con).paraManutencao());
    }

    /** @return lojas ativas @throws NegocioException em erro de banco */
    public List<Loja> lojas() throws NegocioException { return executar(con -> new LojaDAO(con).listarAtivas()); }

    /** @return categorias @throws NegocioException em erro de banco */
    public List<CategoriaVeiculo> categorias() throws NegocioException {
        return executar(con -> new CategoriaDAO(con).listar());
    }

    /**
     * Cadastra um veículo novo.
     *
     * @param v dados do veículo (placa no formato antigo ou Mercosul)
     * @throws NegocioException se algum dado for inválido ou a placa já existir (RN02)
     */
    public void cadastrarVeiculo(Veiculo v) throws NegocioException {
        v.setPlaca(v.getPlaca() == null ? "" : v.getPlaca().trim().toUpperCase().replace("-", ""));
        if (!v.getPlaca().matches("[A-Z]{3}[0-9][A-Z0-9][0-9]{2}")) throw new NegocioException("Placa inválida.");
        if (v.getMarca() == null || v.getMarca().isBlank() || v.getModelo() == null || v.getModelo().isBlank())
            throw new NegocioException("Informe marca e modelo.");
        if (v.getCapacidadeTanqueLitros() <= 0) throw new NegocioException("Informe a capacidade do tanque em litros.");
        executarSemRetorno(con -> { new VeiculoDAO(con).inserir(v); return null; });
    }

    /**
     * Desativa ou reativa um veículo.
     *
     * @param id     código do veículo
     * @param status DISPONIVEL ou INATIVO
     * @throws NegocioException se o veículo estiver locado ou em manutenção
     */
    public void alterarStatusVeiculo(int id, StatusVeiculo status) throws NegocioException {
        boolean ok = executar(con -> new VeiculoDAO(con).alterarStatus(id, status));
        if (!ok) throw new NegocioException("Veículo locado ou em manutenção não pode ser desativado.");
    }

    /**
     * Altera os preços de uma categoria (a mudança é auditada por trigger, RF17).
     *
     * @param c categoria com os novos valores
     * @throws NegocioException se algum valor for inválido
     */
    public void atualizarPrecos(CategoriaVeiculo c) throws NegocioException {
        if (c.getValorDiaria().signum() <= 0) throw new NegocioException("A diária deve ser maior que zero.");
        if (c.getValorHoraExtra().signum() < 0 || c.getTaxaDevolucaoOutraLoja().signum() < 0
                || c.getValorCautela().signum() < 0) throw new NegocioException("Valores não podem ser negativos.");
        executarSemRetorno(con -> { new CategoriaDAO(con).atualizarPrecos(c); return null; });
    }

    /** @param emAndamento só as abertas @return manutenções @throws NegocioException em erro de banco */
    public Tabela manutencoes(boolean emAndamento) throws NegocioException {
        return executar(con -> new ManutencaoDAO(con).listar(emAndamento));
    }

    /**
     * Abre uma manutenção; o veículo fica indisponível até a conclusão.
     *
     * @param m veículo, tipo e descrição
     * @throws NegocioException se o veículo estiver locado ou os dados forem inválidos
     */
    public void abrirManutencao(Manutencao m) throws NegocioException {
        if (m.getDescricao() == null || m.getDescricao().isBlank()) throw new NegocioException("Descreva a manutenção.");
        executarSemRetorno(con -> { new ManutencaoDAO(con).abrir(m, gerente.getId()); return null; });
    }

    /**
     * Conclui uma manutenção e libera o veículo.
     *
     * @param id    código da manutenção
     * @param custo custo total
     * @throws NegocioException se o custo for inválido
     */
    public void concluirManutencao(int id, BigDecimal custo) throws NegocioException {
        if (custo == null || custo.signum() < 0) throw new NegocioException("Informe o custo (zero ou mais).");
        executarSemRetorno(con -> { new ManutencaoDAO(con).concluir(id, custo); return null; });
    }

    /** @return funcionários @throws NegocioException em erro de banco */
    public Tabela funcionarios() throws NegocioException { return executar(con -> new FuncionarioDAO(con).listar()); }

    /**
     * Cadastra um funcionário (RN10: só o gerente).
     *
     * @param f     dados do funcionário
     * @param senha senha inicial (mínimo de 6 caracteres)
     * @throws NegocioException se algum dado for inválido
     */
    public void cadastrarFuncionario(Funcionario f, String senha) throws NegocioException {
        if (f.getNome() == null || f.getNome().isBlank()) throw new NegocioException("Informe o nome.");
        if (f.getEmail() == null || !f.getEmail().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) throw new NegocioException("E-mail inválido.");
        if (f.getCpf() == null || !f.getCpf().matches("\\d{11}")) throw new NegocioException("O CPF deve ter 11 números.");
        if (senha == null || senha.length() < 6) throw new NegocioException("A senha deve ter pelo menos 6 caracteres.");
        executarSemRetorno(con -> { new FuncionarioDAO(con).inserir(f, senha); return null; });
    }

    /**
     * Ativa ou desativa um funcionário. O gerente não pode desativar a si mesmo.
     *
     * @param id    código do funcionário
     * @param ativo nova situação
     * @throws NegocioException se tentar desativar a si mesmo
     */
    public void alterarAtivo(int id, boolean ativo) throws NegocioException {
        if (id == gerente.getId() && !ativo) throw new NegocioException("Você não pode desativar o próprio usuário.");
        executarSemRetorno(con -> { new FuncionarioDAO(con).alterarAtivo(id, ativo); return null; });
    }

    /** @return faturamento mensal @throws NegocioException em erro de banco */
    public Tabela faturamentoMensal() throws NegocioException { return executar(con -> new RelatorioDAO(con).faturamentoMensal()); }

    /** @return últimos registros de auditoria @throws NegocioException em erro de banco */
    public Tabela auditoria() throws NegocioException { return executar(con -> new RelatorioDAO(con).auditoria()); }
}
