package br.edu.unoesc.nextdrive.dao;

import br.edu.unoesc.nextdrive.model.Manutencao;
import br.edu.unoesc.nextdrive.util.Tabela;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Acesso às manutenções. Os triggers do banco ajustam a situação do veículo (RN04, RN11). */
public class ManutencaoDAO {
    private final Connection con;

    public ManutencaoDAO(Connection con) { this.con = con; }

    /**
     * Abre uma manutenção com entrada hoje; o veículo passa a ficar em manutenção.
     *
     * @param m             dados (veículo, tipo e descrição)
     * @param funcionarioId quem registrou
     * @throws SQLException se o veículo estiver locado ou o dado for inválido
     */
    public void abrir(Manutencao m, int funcionarioId) throws SQLException {
        String sql = "INSERT INTO manutencao (veiculo_id, funcionario_id, tipo, descricao, data_entrada) "
                   + "VALUES (?, ?, ?, ?, CURRENT_DATE)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, m.getVeiculoId());
            ps.setInt(2, funcionarioId);
            ps.setString(3, m.getTipo().name());
            ps.setString(4, m.getDescricao());
            ps.executeUpdate();
        }
    }

    /**
     * Conclui uma manutenção em andamento, com saída hoje; o veículo volta a ficar disponível.
     *
     * @param id    código da manutenção
     * @param custo custo total
     * @throws SQLException em erro de banco
     */
    public void concluir(int id, BigDecimal custo) throws SQLException {
        String sql = "UPDATE manutencao SET status = 'CONCLUIDA', data_saida = CURRENT_DATE, custo = ? "
                   + "WHERE id = ? AND status = 'EM_ANDAMENTO'";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBigDecimal(1, custo);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    /**
     * @param somenteEmAndamento true para listar só as abertas
     * @return colunas: id, placa, veiculo, tipo, descricao, entrada, saida, custo, status
     * @throws SQLException em erro de banco
     */
    public Tabela listar(boolean somenteEmAndamento) throws SQLException {
        String sql = "SELECT m.id, v.placa, v.marca || ' ' || v.modelo AS veiculo, m.tipo, m.descricao, "
                   + "m.data_entrada AS entrada, m.data_saida AS saida, m.custo, m.status "
                   + "FROM manutencao m JOIN veiculo v ON v.id = m.veiculo_id "
                   + (somenteEmAndamento ? "WHERE m.status = 'EM_ANDAMENTO' " : "")
                   + "ORDER BY m.id DESC";
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return Tabela.de(rs);
        }
    }
}
