package br.edu.unoesc.nextdrive.service;

import br.edu.unoesc.nextdrive.dao.ClienteDAO;
import br.edu.unoesc.nextdrive.dao.PessoaDAO;
import br.edu.unoesc.nextdrive.model.ClientePessoaFisica;
import br.edu.unoesc.nextdrive.model.ClientePessoaJuridica;
import br.edu.unoesc.nextdrive.model.Usuario;
import br.edu.unoesc.nextdrive.util.Conexao.Papel;
import br.edu.unoesc.nextdrive.util.Tabela;
import java.util.Optional;

/** Login (RF02) e cadastro de clientes (RF04), pelo site ou pelo balcão. */
public class AutenticacaoService extends Servico {

    /** Serviço para visitantes (login e autocadastro). */
    public AutenticacaoService() { super(Papel.SITE); }

    /** Serviço para um funcionário cadastrando clientes no balcão. */
    public AutenticacaoService(Usuario funcionario) { super(papelDe(funcionario)); }

    /**
     * @param email e-mail digitado
     * @param senha senha digitada
     * @return o usuário autenticado, ou vazio se as credenciais estiverem erradas
     * @throws NegocioException se o banco não responder
     */
    public Optional<Usuario> login(String email, String senha) throws NegocioException {
        if (email == null || email.isBlank() || senha == null || senha.isEmpty()) return Optional.empty();
        return executar(con -> new PessoaDAO(con).autenticar(email.trim(), senha));
    }

    /**
     * Cadastra um cliente pessoa física.
     *
     * @param pf    dados do cliente
     * @param senha senha escolhida (mínimo de 6 caracteres)
     * @return o código do novo cliente
     * @throws NegocioException se algum dado for inválido ou já estiver cadastrado
     */
    public int cadastrarPF(ClientePessoaFisica pf, String senha) throws NegocioException {
        validarBase(pf.getNome(), pf.getEmail(), senha);
        if (!pf.getCpf().matches("\\d{11}")) throw new NegocioException("O CPF deve ter 11 números.");
        if (!pf.getCnhNumero().matches("\\d{11}")) throw new NegocioException("A CNH deve ter 11 números.");
        if (!pf.getCnhValidade().isAfter(pf.getCnhDataEmissao()))
            throw new NegocioException("A validade da CNH deve ser depois da emissão.");
        if (!pf.getCnhDataEmissao().isAfter(pf.getDataNascimento()))
            throw new NegocioException("A CNH não pode ser emitida antes do nascimento.");
        return executar(con -> new ClienteDAO(con).inserirPF(pf, senha));
    }

    /**
     * Cadastra um cliente pessoa jurídica com o seu condutor responsável.
     *
     * @param pj    dados da empresa (condutor responsável já preenchido com o código)
     * @param senha senha inicial (mínimo de 6 caracteres)
     * @return o código do novo cliente
     * @throws NegocioException se algum dado for inválido ou já estiver cadastrado
     */
    public int cadastrarPJ(ClientePessoaJuridica pj, String senha) throws NegocioException {
        validarBase(pj.getNome(), pj.getEmail(), senha);
        if (!pj.getCnpj().matches("\\d{14}")) throw new NegocioException("O CNPJ deve ter 14 números.");
        if (pj.getRazaoSocial() == null || pj.getRazaoSocial().isBlank()) throw new NegocioException("Informe a razão social.");
        return executar(con -> new ClienteDAO(con).inserirPJ(pj, senha));
    }

    /**
     * @return clientes cadastrados (id, nome, tipo, documento)
     * @throws NegocioException em erro de banco
     */
    public Tabela clientes() throws NegocioException { return executar(con -> new ClienteDAO(con).listar()); }

    /**
     * @return pessoas físicas que podem ser condutor responsável (id, nome, documento)
     * @throws NegocioException em erro de banco
     */
    public Tabela condutores() throws NegocioException { return executar(con -> new ClienteDAO(con).listarCondutores()); }

    /**
     * @param clienteId código do cliente
     * @return histórico de locações (RF13)
     * @throws NegocioException em erro de banco
     */
    public Tabela historico(int clienteId) throws NegocioException {
        return executar(con -> new ClienteDAO(con).historico(clienteId));
    }

    private void validarBase(String nome, String email, String senha) throws NegocioException {
        if (nome == null || nome.isBlank()) throw new NegocioException("Informe o nome.");
        if (email == null || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) throw new NegocioException("E-mail inválido.");
        if (senha == null || senha.length() < 6) throw new NegocioException("A senha deve ter pelo menos 6 caracteres.");
    }
}
