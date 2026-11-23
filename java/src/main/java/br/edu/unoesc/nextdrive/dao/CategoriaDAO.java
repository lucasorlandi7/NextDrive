package br.edu.unoesc.nextdrive.dao;

import br.edu.unoesc.nextdrive.model.Categoria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Acesso à tabela categoria. */
public class CategoriaDAO {

    private Connection connection = SingleConnection.getConnection();

    /**
     * Lista todas as categorias.
     *
     * @return lista de categorias ordenada pelo código
     */
    public List<Categoria> listar() {
        List<Categoria> lista = new ArrayList<Categoria>();
        String sql = "SELECT id, nome, valor_diaria FROM categoria ORDER BY id";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(new Categoria(rs.getInt("id"), rs.getString("nome"),
                        rs.getBigDecimal("valor_diaria")));
            }
            rs.close();
            ps.close();
            connection.commit();
        } catch (SQLException e) {
            SingleConnection.erro("listar as categorias", e);
        }
        return lista;
    }

    /**
     * Busca uma categoria pelo código.
     *
     * @param id código da categoria
     * @return a categoria, ou null se não existir
     */
    public Categoria buscar(int id) {
        Categoria categoria = null;
        String sql = "SELECT id, nome, valor_diaria FROM categoria WHERE id = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                categoria = new Categoria(rs.getInt("id"), rs.getString("nome"),
                        rs.getBigDecimal("valor_diaria"));
            }
            rs.close();
            ps.close();
            connection.commit();
        } catch (SQLException e) {
            SingleConnection.erro("buscar a categoria", e);
        }
        return categoria;
    }
}
