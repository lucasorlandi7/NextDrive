package br.edu.unoesc.nextdrive.dao;

import br.edu.unoesc.nextdrive.model.Loja;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Acesso às lojas. */
public class LojaDAO {
    private final Connection con;

    public LojaDAO(Connection con) { this.con = con; }

    /**
     * @return as lojas ativas, em ordem de nome
     * @throws SQLException em erro de banco
     */
    public List<Loja> listarAtivas() throws SQLException {
        List<Loja> lojas = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT id, nome, cidade, uf FROM loja WHERE ativa ORDER BY nome");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lojas.add(new Loja(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4)));
        }
        return lojas;
    }
}
