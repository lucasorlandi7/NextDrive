package br.edu.unoesc.nextdrive.dao;

import br.edu.unoesc.nextdrive.model.CategoriaVeiculo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Acesso às categorias de veículos e aos seus preços. */
public class CategoriaDAO {
    private static final String COLUNAS =
            "id, nome, descricao, valor_diaria, valor_hora_extra, taxa_devolucao_outra_loja, valor_cautela";
    private final Connection con;

    public CategoriaDAO(Connection con) { this.con = con; }

    /**
     * @return todas as categorias, em ordem de nome
     * @throws SQLException em erro de banco
     */
    public List<CategoriaVeiculo> listar() throws SQLException {
        List<CategoriaVeiculo> lista = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement("SELECT " + COLUNAS + " FROM categoria_veiculo ORDER BY nome");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    /**
     * @param id código da categoria
     * @return a categoria, se existir
     * @throws SQLException em erro de banco
     */
    public Optional<CategoriaVeiculo> buscar(int id) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT " + COLUNAS + " FROM categoria_veiculo WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    /**
     * Atualiza os preços de uma categoria. A mudança é registrada na auditoria por trigger (RF17).
     *
     * @param c categoria com os novos valores
     * @throws SQLException se algum valor violar as regras do banco
     */
    public void atualizarPrecos(CategoriaVeiculo c) throws SQLException {
        String sql = "UPDATE categoria_veiculo SET valor_diaria = ?, valor_hora_extra = ?, "
                   + "taxa_devolucao_outra_loja = ?, valor_cautela = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBigDecimal(1, c.getValorDiaria());
            ps.setBigDecimal(2, c.getValorHoraExtra());
            ps.setBigDecimal(3, c.getTaxaDevolucaoOutraLoja());
            ps.setBigDecimal(4, c.getValorCautela());
            ps.setInt(5, c.getId());
            ps.executeUpdate();
        }
    }

    private CategoriaVeiculo mapear(ResultSet rs) throws SQLException {
        CategoriaVeiculo c = new CategoriaVeiculo();
        c.setId(rs.getInt("id"));
        c.setNome(rs.getString("nome"));
        c.setDescricao(rs.getString("descricao"));
        c.setValorDiaria(rs.getBigDecimal("valor_diaria"));
        c.setValorHoraExtra(rs.getBigDecimal("valor_hora_extra"));
        c.setTaxaDevolucaoOutraLoja(rs.getBigDecimal("taxa_devolucao_outra_loja"));
        c.setValorCautela(rs.getBigDecimal("valor_cautela"));
        return c;
    }
}
