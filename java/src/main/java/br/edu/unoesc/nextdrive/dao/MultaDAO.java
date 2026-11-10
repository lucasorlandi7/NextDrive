package br.edu.unoesc.nextdrive.dao;

import br.edu.unoesc.nextdrive.model.Multa;
import br.edu.unoesc.nextdrive.util.Tabela;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/** Acesso às multas de trânsito. */
public class MultaDAO {
    private final Connection con;

    public MultaDAO(Connection con) { this.con = con; }

    /**
     * Registra uma multa recebida depois da locação (RF24).
     *
     * @param m             multa validada
     * @param funcionarioId quem registrou
     * @throws SQLException se a locação não existir ou algum dado violar as regras
     */
    public void inserir(Multa m, int funcionarioId) throws SQLException {
        String sql = "INSERT INTO multa (locacao_id, descricao, valor, data_infracao, funcionario_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, m.getLocacaoId());
            ps.setString(2, m.getDescricao());
            ps.setBigDecimal(3, m.getValor());
            ps.setTimestamp(4, Timestamp.valueOf(m.getDataInfracao()));
            ps.setInt(5, funcionarioId);
            ps.executeUpdate();
        }
    }

    /**
     * Marca a multa como paga. Quando é a última pendente, o trigger libera a cautela.
     *
     * @param multaId código da multa
     * @throws SQLException em erro de banco
     */
    public void pagar(int multaId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("UPDATE multa SET status = 'PAGA' WHERE id = ? AND status = 'PENDENTE'")) {
            ps.setInt(1, multaId);
            ps.executeUpdate();
        }
    }

    /**
     * @return multas pendentes, da view {@code vw_multas_pendentes}
     * @throws SQLException em erro de banco
     */
    public Tabela pendentes() throws SQLException {
        String sql = "SELECT multa_id AS id, cliente, placa, descricao, data_infracao AS infracao, valor, "
                   + "locacao_id AS locacao, cautela_status AS cautela FROM vw_multas_pendentes ORDER BY multa_id";
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return Tabela.de(rs);
        }
    }
}
