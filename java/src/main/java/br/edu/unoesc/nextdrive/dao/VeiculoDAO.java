package br.edu.unoesc.nextdrive.dao;

import br.edu.unoesc.nextdrive.model.Categoria;
import br.edu.unoesc.nextdrive.model.StatusVeiculo;
import br.edu.unoesc.nextdrive.model.Veiculo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Acesso à tabela veiculo: salvar, listar, buscar, atualizar e excluir. */
public class VeiculoDAO {

    private static final String SELECT = "SELECT v.id, v.placa, v.modelo, v.ano, v.quilometragem, "
            + "v.status, c.id AS cat_id, c.nome AS cat_nome, c.valor_diaria "
            + "FROM veiculo v JOIN categoria c ON c.id = v.categoria_id ";

    private Connection connection = SingleConnection.getConnection();

    /**
     * Salva um veículo novo.
     *
     * @param v veículo a salvar
     * @return true se salvou, false se deu erro (por exemplo, placa repetida)
     */
    public boolean salvar(Veiculo v) {
        String sql = "INSERT INTO veiculo (placa, modelo, ano, quilometragem, status, categoria_id) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, v.getPlaca());
            ps.setString(2, v.getModelo());
            ps.setInt(3, v.getAno());
            ps.setInt(4, v.getQuilometragem());
            ps.setString(5, v.getStatus().name());
            ps.setInt(6, v.getCategoria().getId());
            ps.execute();
            ps.close();
            connection.commit();
            return true;
        } catch (SQLException e) {
            SingleConnection.erro("salvar o veículo", e);
            return false;
        }
    }

    /**
     * Lista todos os veículos.
     *
     * @return lista de veículos ordenada pelo código
     */
    public List<Veiculo> listar() {
        return consultar(SELECT + "ORDER BY v.id", 0);
    }

    /**
     * Busca um veículo pelo código.
     *
     * @param id código do veículo
     * @return o veículo, ou null se não existir
     */
    public Veiculo buscar(int id) {
        List<Veiculo> lista = consultar(SELECT + "WHERE v.id = ?", id);
        if (lista.isEmpty()) {
            return null;
        }
        return lista.get(0);
    }

    /**
     * Atualiza modelo, quilometragem e situação de um veículo.
     *
     * @param v veículo com os dados novos (o código identifica quem será alterado)
     * @return true se atualizou
     */
    public boolean atualizar(Veiculo v) {
        String sql = "UPDATE veiculo SET modelo = ?, quilometragem = ?, status = ? WHERE id = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, v.getModelo());
            ps.setInt(2, v.getQuilometragem());
            ps.setString(3, v.getStatus().name());
            ps.setInt(4, v.getId());
            ps.execute();
            ps.close();
            connection.commit();
            return true;
        } catch (SQLException e) {
            SingleConnection.erro("atualizar o veículo", e);
            return false;
        }
    }

    /**
     * Exclui um veículo. Não funciona se ele já foi usado em uma locação.
     *
     * @param id código do veículo
     * @return true se excluiu
     */
    public boolean excluir(int id) {
        String sql = "DELETE FROM veiculo WHERE id = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, id);
            int linhas = ps.executeUpdate();
            ps.close();
            connection.commit();
            return linhas > 0;
        } catch (SQLException e) {
            SingleConnection.erro("excluir o veículo", e);
            return false;
        }
    }

    /** Executa a consulta; se o parâmetro for diferente de zero, ele entra no primeiro "?". */
    private List<Veiculo> consultar(String sql, int parametro) {
        List<Veiculo> lista = new ArrayList<Veiculo>();
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            if (parametro != 0) {
                ps.setInt(1, parametro);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Categoria categoria = new Categoria(rs.getInt("cat_id"), rs.getString("cat_nome"),
                        rs.getBigDecimal("valor_diaria"));
                Veiculo v = new Veiculo(rs.getString("placa"), rs.getString("modelo"),
                        rs.getInt("ano"), rs.getInt("quilometragem"), categoria);
                v.setId(rs.getInt("id"));
                v.setStatus(StatusVeiculo.valueOf(rs.getString("status")));
                lista.add(v);
            }
            rs.close();
            ps.close();
            connection.commit();
        } catch (SQLException e) {
            SingleConnection.erro("consultar os veículos", e);
        }
        return lista;
    }
}
