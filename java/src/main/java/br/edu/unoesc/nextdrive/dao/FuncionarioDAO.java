package br.edu.unoesc.nextdrive.dao;

import br.edu.unoesc.nextdrive.model.Funcionario;
import br.edu.unoesc.nextdrive.util.Tabela;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Acesso aos funcionários (somente o gerente grava, RN10). */
public class FuncionarioDAO {
    private final Connection con;

    public FuncionarioDAO(Connection con) { this.con = con; }

    /**
     * @return colunas: id, nome, email, cargo, loja, ativo
     * @throws SQLException em erro de banco
     */
    public Tabela listar() throws SQLException {
        String sql = "SELECT p.id, p.nome, p.email, f.cargo, l.nome AS loja, f.ativo "
                   + "FROM funcionario f JOIN pessoa p ON p.id = f.pessoa_id JOIN loja l ON l.id = f.loja_id "
                   + "ORDER BY p.nome";
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return Tabela.de(rs);
        }
    }

    /**
     * Cadastra um funcionário (pessoa + funcionario) em uma transação.
     *
     * @param f     dados do funcionário
     * @param senha senha inicial em texto, guardada com bcrypt
     * @return o código gerado
     * @throws SQLException se algum dado violar as regras do banco
     */
    public int inserir(Funcionario f, String senha) throws SQLException {
        boolean auto = con.getAutoCommit();
        con.setAutoCommit(false);
        try {
            int id;
            String sqlPessoa = "INSERT INTO pessoa (nome, email, senha_hash, telefone) "
                             + "VALUES (?, ?, crypt(?, gen_salt('bf')), ?) RETURNING id";
            try (PreparedStatement ps = con.prepareStatement(sqlPessoa)) {
                ps.setString(1, f.getNome());
                ps.setString(2, f.getEmail());
                ps.setString(3, senha);
                ps.setString(4, f.getTelefone());
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    id = rs.getInt(1);
                }
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO funcionario (pessoa_id, cpf, cargo, loja_id) VALUES (?, ?, ?, ?)")) {
                ps.setInt(1, id);
                ps.setString(2, f.getCpf());
                ps.setString(3, f.getCargo().name());
                ps.setInt(4, f.getLojaId());
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
     * Ativa ou desativa um funcionário (quem está inativo não consegue entrar no sistema).
     *
     * @param id    código do funcionário
     * @param ativo nova situação
     * @throws SQLException em erro de banco
     */
    public void alterarAtivo(int id, boolean ativo) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("UPDATE funcionario SET ativo = ? WHERE pessoa_id = ?")) {
            ps.setBoolean(1, ativo);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }
}
