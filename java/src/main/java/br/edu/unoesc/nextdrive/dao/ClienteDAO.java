package br.edu.unoesc.nextdrive.dao;

import br.edu.unoesc.nextdrive.model.Cliente;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Acesso à tabela cliente: salvar, listar, buscar, atualizar e excluir. */
public class ClienteDAO {

    private Connection connection = SingleConnection.getConnection();

    /**
     * Salva um cliente novo.
     *
     * @param c cliente a salvar
     * @return true se salvou, false se deu erro (por exemplo, CPF repetido)
     */
    public boolean salvar(Cliente c) {
        String sql = "INSERT INTO cliente (nome, cpf, email, telefone, cnh_numero, cnh_validade) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, c.getNome());
            ps.setString(2, c.getCpf());
            ps.setString(3, c.getEmail());
            ps.setString(4, c.getTelefone());
            ps.setString(5, c.getCnhNumero());
            ps.setDate(6, Date.valueOf(c.getCnhValidade()));
            ps.execute();
            ps.close();
            connection.commit();
            return true;
        } catch (SQLException e) {
            SingleConnection.erro("salvar o cliente", e);
            return false;
        }
    }

    /**
     * Lista todos os clientes.
     *
     * @return lista de clientes ordenada pelo código
     */
    public List<Cliente> listar() {
        List<Cliente> lista = new ArrayList<Cliente>();
        String sql = "SELECT id, nome, cpf, email, telefone, cnh_numero, cnh_validade "
                + "FROM cliente ORDER BY id";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(montar(rs));
            }
            rs.close();
            ps.close();
            connection.commit();
        } catch (SQLException e) {
            SingleConnection.erro("listar os clientes", e);
        }
        return lista;
    }

    /**
     * Busca um cliente pelo código.
     *
     * @param id código do cliente
     * @return o cliente, ou null se não existir
     */
    public Cliente buscar(int id) {
        Cliente cliente = null;
        String sql = "SELECT id, nome, cpf, email, telefone, cnh_numero, cnh_validade "
                + "FROM cliente WHERE id = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                cliente = montar(rs);
            }
            rs.close();
            ps.close();
            connection.commit();
        } catch (SQLException e) {
            SingleConnection.erro("buscar o cliente", e);
        }
        return cliente;
    }

    /**
     * Exclui um cliente. Não funciona se ele tiver reservas.
     *
     * @param id código do cliente
     * @return true se excluiu
     */
    public boolean excluir(int id) {
        String sql = "DELETE FROM cliente WHERE id = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, id);
            int linhas = ps.executeUpdate();
            ps.close();
            connection.commit();
            return linhas > 0;
        } catch (SQLException e) {
            SingleConnection.erro("excluir o cliente", e);
            return false;
        }
    }

    private Cliente montar(ResultSet rs) throws SQLException {
        Cliente c = new Cliente(rs.getString("nome"), rs.getString("cpf"), rs.getString("email"),
                rs.getString("telefone"), rs.getString("cnh_numero"),
                rs.getDate("cnh_validade").toLocalDate());
        c.setId(rs.getInt("id"));
        return c;
    }
}
