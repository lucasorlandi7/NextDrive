package br.edu.unoesc.nextdrive.dao;

import br.edu.unoesc.nextdrive.model.Categoria;
import br.edu.unoesc.nextdrive.model.Cliente;
import br.edu.unoesc.nextdrive.model.Reserva;
import br.edu.unoesc.nextdrive.model.StatusReserva;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Acesso à tabela reserva. */
public class ReservaDAO {

    private Connection connection = SingleConnection.getConnection();

    /**
     * Busca uma reserva pelo código, já com o cliente e a categoria.
     *
     * @param id código da reserva
     * @return a reserva, ou null se não existir
     */
    public Reserva buscar(int id) {
        Reserva reserva = null;
        String sql = "SELECT r.id, r.data_retirada_prevista, r.data_devolucao_prevista, r.status, "
                + "cl.id AS cli_id, cl.nome, cl.cpf, cl.email, cl.telefone, cl.cnh_numero, cl.cnh_validade, "
                + "ca.id AS cat_id, ca.nome AS cat_nome, ca.valor_diaria "
                + "FROM reserva r JOIN cliente cl ON cl.id = r.cliente_id "
                + "JOIN categoria ca ON ca.id = r.categoria_id WHERE r.id = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Cliente cliente = new Cliente(rs.getString("nome"), rs.getString("cpf"),
                        rs.getString("email"), rs.getString("telefone"), rs.getString("cnh_numero"),
                        rs.getDate("cnh_validade").toLocalDate());
                cliente.setId(rs.getInt("cli_id"));
                Categoria categoria = new Categoria(rs.getInt("cat_id"), rs.getString("cat_nome"),
                        rs.getBigDecimal("valor_diaria"));
                reserva = new Reserva(cliente, categoria,
                        rs.getDate("data_retirada_prevista").toLocalDate(),
                        rs.getDate("data_devolucao_prevista").toLocalDate());
                reserva.setId(rs.getInt("id"));
                reserva.setStatus(StatusReserva.valueOf(rs.getString("status")));
            }
            rs.close();
            ps.close();
            connection.commit();
        } catch (SQLException e) {
            SingleConnection.erro("buscar a reserva", e);
        }
        return reserva;
    }
}
