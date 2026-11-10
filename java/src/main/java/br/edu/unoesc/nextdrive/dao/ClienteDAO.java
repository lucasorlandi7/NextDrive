package br.edu.unoesc.nextdrive.dao;

import br.edu.unoesc.nextdrive.model.ClientePessoaFisica;
import br.edu.unoesc.nextdrive.model.ClientePessoaJuridica;
import br.edu.unoesc.nextdrive.util.Tabela;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/** Acesso aos dados de clientes (pessoa, cliente, cliente_pf e cliente_pj). */
public class ClienteDAO {
    private final Connection con;

    public ClienteDAO(Connection con) { this.con = con; }

    /**
     * Grava um cliente pessoa física nas três tabelas, em uma única transação.
     *
     * @param pf    dados do cliente
     * @param senha senha em texto, guardada com bcrypt
     * @return o código gerado
     * @throws SQLException se algum dado violar as regras do banco (CPF/CNH/e-mail repetidos etc.)
     */
    public int inserirPF(ClientePessoaFisica pf, String senha) throws SQLException {
        boolean auto = con.getAutoCommit();
        con.setAutoCommit(false);
        try {
            int id = inserirPessoaECliente(pf.getNome(), pf.getEmail(), pf.getTelefone(), senha, "PF");
            String sql = "INSERT INTO cliente_pf (pessoa_id, cpf, data_nascimento, cnh_numero, cnh_data_emissao, cnh_validade) "
                       + "VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, id);
                ps.setString(2, pf.getCpf());
                ps.setDate(3, Date.valueOf(pf.getDataNascimento()));
                ps.setString(4, pf.getCnhNumero());
                ps.setDate(5, Date.valueOf(pf.getCnhDataEmissao()));
                ps.setDate(6, Date.valueOf(pf.getCnhValidade()));
                ps.executeUpdate();
            }
            con.commit();
            return id;
        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(auto);
        }
    }

    /**
     * Grava um cliente pessoa jurídica, ligado a um condutor responsável já cadastrado.
     *
     * @param pj    dados da empresa (com o condutor responsável preenchido)
     * @param senha senha em texto, guardada com bcrypt
     * @return o código gerado
     * @throws SQLException se algum dado violar as regras do banco
     */
    public int inserirPJ(ClientePessoaJuridica pj, String senha) throws SQLException {
        boolean auto = con.getAutoCommit();
        con.setAutoCommit(false);
        try {
            int id = inserirPessoaECliente(pj.getNome(), pj.getEmail(), pj.getTelefone(), senha, "PJ");
            String sql = "INSERT INTO cliente_pj (pessoa_id, cnpj, razao_social, condutor_id) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, id);
                ps.setString(2, pj.getCnpj());
                ps.setString(3, pj.getRazaoSocial());
                ps.setInt(4, pj.getCondutorResponsavel().getId());
                ps.executeUpdate();
            }
            con.commit();
            return id;
        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(auto);
        }
    }

    private int inserirPessoaECliente(String nome, String email, String telefone, String senha, String tipo)
            throws SQLException {
        String sqlPessoa = "INSERT INTO pessoa (nome, email, senha_hash, telefone) "
                         + "VALUES (?, ?, crypt(?, gen_salt('bf')), ?) RETURNING id";
        int id;
        try (PreparedStatement ps = con.prepareStatement(sqlPessoa)) {
            ps.setString(1, nome);
            ps.setString(2, email);
            ps.setString(3, senha);
            ps.setString(4, telefone);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                id = rs.getInt(1);
            }
        }
        try (PreparedStatement ps = con.prepareStatement("INSERT INTO cliente (pessoa_id, tipo_cliente) VALUES (?, ?)")) {
            ps.setInt(1, id);
            ps.setString(2, tipo);
            ps.executeUpdate();
        }
        return id;
    }

    /**
     * Lista todos os clientes, para seleção no balcão.
     *
     * @return colunas: id, nome, tipo, documento
     * @throws SQLException em erro de banco
     */
    public Tabela listar() throws SQLException {
        String sql = "SELECT p.id, p.nome, c.tipo_cliente AS tipo, COALESCE(pf.cpf, pj.cnpj) AS documento "
                   + "FROM cliente c JOIN pessoa p ON p.id = c.pessoa_id "
                   + "LEFT JOIN cliente_pf pf ON pf.pessoa_id = c.pessoa_id "
                   + "LEFT JOIN cliente_pj pj ON pj.pessoa_id = c.pessoa_id ORDER BY p.nome";
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return Tabela.de(rs);
        }
    }

    /**
     * Lista as pessoas físicas, que podem ser condutor responsável de uma empresa.
     *
     * @return colunas: id, nome, documento
     * @throws SQLException em erro de banco
     */
    public Tabela listarCondutores() throws SQLException {
        String sql = "SELECT p.id, p.nome, pf.cpf AS documento FROM cliente_pf pf JOIN pessoa p ON p.id = pf.pessoa_id ORDER BY p.nome";
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return Tabela.de(rs);
        }
    }

    /**
     * Histórico de locações de um cliente (RF13).
     *
     * @param clienteId código do cliente
     * @return linhas da view {@code vw_historico_cliente}
     * @throws SQLException em erro de banco
     */
    public Tabela historico(int clienteId) throws SQLException {
        String sql = "SELECT locacao_id AS locacao, placa, veiculo, data_retirada AS retirada, "
                   + "data_devolucao_real AS devolucao, valor_final AS valor, status "
                   + "FROM vw_historico_cliente WHERE cliente_id = ? ORDER BY data_retirada DESC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) {
                return Tabela.de(rs);
            }
        }
    }

    /**
     * Encontra quem vai dirigir na reserva: o próprio cliente (pessoa física) ou o condutor
     * responsável da empresa (RF04, RF16).
     *
     * @param reservaId código da reserva
     * @return o condutor, se a reserva existir
     * @throws SQLException em erro de banco
     */
    public Optional<ClientePessoaFisica> condutorDaReserva(int reservaId) throws SQLException {
        String sql = "SELECT p.id, p.nome, p.email, p.telefone, pf.cpf, pf.data_nascimento, pf.cnh_numero, "
                   + "pf.cnh_data_emissao, pf.cnh_validade "
                   + "FROM reserva r LEFT JOIN cliente_pj pj ON pj.pessoa_id = r.cliente_id "
                   + "JOIN cliente_pf pf ON pf.pessoa_id = COALESCE(pj.condutor_id, r.cliente_id) "
                   + "JOIN pessoa p ON p.id = pf.pessoa_id WHERE r.id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, reservaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(new ClientePessoaFisica(rs.getInt(1), rs.getString(2), rs.getString(3),
                        rs.getString(4), rs.getString(5), rs.getDate(6).toLocalDate(), rs.getString(7),
                        rs.getDate(8).toLocalDate(), rs.getDate(9).toLocalDate()));
            }
        }
    }
}
