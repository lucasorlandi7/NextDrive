package br.edu.unoesc.nextdrive.dao;

import br.edu.unoesc.nextdrive.model.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/** Acesso à autenticação de usuários (função {@code fn_autenticar}, senha com bcrypt). */
public class PessoaDAO {
    private final Connection con;

    public PessoaDAO(Connection con) { this.con = con; }

    /**
     * Confere e-mail e senha no banco; a senha nunca sai do banco (RNF05).
     *
     * @param email e-mail digitado
     * @param senha senha digitada
     * @return o usuário, se as credenciais forem válidas
     * @throws SQLException em erro de banco
     */
    public Optional<Usuario> autenticar(String email, String senha) throws SQLException {
        String sql = "SELECT pessoa_id, nome, perfil FROM fn_autenticar(?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, senha);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(new Usuario(rs.getInt(1), rs.getString(2), rs.getString(3)));
            }
        }
    }
}
