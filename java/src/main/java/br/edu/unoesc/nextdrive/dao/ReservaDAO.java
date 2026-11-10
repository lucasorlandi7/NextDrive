package br.edu.unoesc.nextdrive.dao;

import br.edu.unoesc.nextdrive.model.Canal;
import br.edu.unoesc.nextdrive.model.Reserva;
import br.edu.unoesc.nextdrive.model.StatusReserva;
import br.edu.unoesc.nextdrive.util.Tabela;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

/** Acesso às reservas. */
public class ReservaDAO {
    private final Connection con;

    public ReservaDAO(Connection con) { this.con = con; }

    /**
     * Conta reservas ativas que disputam veículos da categoria, na loja e no período.
     *
     * @return quantidade de reservas ativas conflitantes
     * @throws SQLException em erro de banco
     */
    public int contarAtivasConflitantes(int categoriaId, int lojaRetiradaId, LocalDateTime inicio, LocalDateTime fim)
            throws SQLException {
        String sql = "SELECT COUNT(*) FROM reserva WHERE status = 'ATIVA' AND categoria_id = ? "
                   + "AND loja_retirada_id = ? AND data_retirada_prevista < ? AND ? < data_devolucao_prevista";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, categoriaId);
            ps.setInt(2, lojaRetiradaId);
            ps.setTimestamp(3, Timestamp.valueOf(fim));
            ps.setTimestamp(4, Timestamp.valueOf(inicio));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /**
     * Grava uma reserva nova.
     *
     * @param r reserva validada
     * @return o código gerado
     * @throws SQLException se algum dado violar as regras do banco
     */
    public int inserir(Reserva r) throws SQLException {
        String sql = "INSERT INTO reserva (cliente_id, categoria_id, loja_retirada_id, loja_devolucao_id, "
                   + "data_retirada_prevista, data_devolucao_prevista, valor_estimado, canal, funcionario_id) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, r.getClienteId());
            ps.setInt(2, r.getCategoriaId());
            ps.setInt(3, r.getLojaRetiradaId());
            ps.setInt(4, r.getLojaDevolucaoId());
            ps.setTimestamp(5, Timestamp.valueOf(r.getDataRetiradaPrevista()));
            ps.setTimestamp(6, Timestamp.valueOf(r.getDataDevolucaoPrevista()));
            ps.setBigDecimal(7, r.getValorEstimado());
            ps.setString(8, r.getCanal().name());
            if (r.getCanal() == Canal.BALCAO) ps.setInt(9, r.getFuncionarioId());
            else ps.setNull(9, java.sql.Types.INTEGER);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /**
     * @param id código da reserva
     * @return a reserva, se existir
     * @throws SQLException em erro de banco
     */
    public Optional<Reserva> buscar(int id) throws SQLException {
        String sql = "SELECT id, cliente_id, categoria_id, loja_retirada_id, loja_devolucao_id, "
                   + "data_retirada_prevista, data_devolucao_prevista, valor_estimado, canal, status "
                   + "FROM reserva WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                Reserva r = new Reserva();
                r.setId(rs.getInt(1));
                r.setClienteId(rs.getInt(2));
                r.setCategoriaId(rs.getInt(3));
                r.setLojaRetiradaId(rs.getInt(4));
                r.setLojaDevolucaoId(rs.getInt(5));
                r.setDataRetiradaPrevista(rs.getTimestamp(6).toLocalDateTime());
                r.setDataDevolucaoPrevista(rs.getTimestamp(7).toLocalDateTime());
                r.setValorEstimado(rs.getBigDecimal(8));
                r.setCanal(Canal.valueOf(rs.getString(9)));
                r.setStatus(StatusReserva.valueOf(rs.getString(10)));
                return Optional.of(r);
            }
        }
    }

    /**
     * Reservas ativas de todos os clientes, para o atendente.
     *
     * @return linhas da view {@code vw_reservas_detalhadas}
     * @throws SQLException em erro de banco
     */
    public Tabela ativas() throws SQLException {
        String sql = "SELECT reserva_id AS id, cliente, documento, categoria, loja_retirada AS retirada_em, "
                   + "loja_devolucao AS devolucao_em, data_retirada_prevista AS retirada, "
                   + "data_devolucao_prevista AS devolucao, valor_estimado AS estimativa, canal "
                   + "FROM vw_reservas_detalhadas WHERE status = 'ATIVA' ORDER BY data_retirada_prevista";
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return Tabela.de(rs);
        }
    }

    /**
     * Reservas de um cliente (todas as situações).
     *
     * @param clienteId código do cliente
     * @return colunas: id, categoria, retirada_em, retirada, devolucao, estimativa, status
     * @throws SQLException em erro de banco
     */
    public Tabela porCliente(int clienteId) throws SQLException {
        String sql = "SELECT r.id, cat.nome AS categoria, lr.nome AS retirada_em, r.data_retirada_prevista AS retirada, "
                   + "r.data_devolucao_prevista AS devolucao, r.valor_estimado AS estimativa, r.status "
                   + "FROM reserva r JOIN categoria_veiculo cat ON cat.id = r.categoria_id "
                   + "JOIN loja lr ON lr.id = r.loja_retirada_id WHERE r.cliente_id = ? ORDER BY r.id DESC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) {
                return Tabela.de(rs);
            }
        }
    }

    /**
     * Cancela a reserva pela procedure {@code sp_cancelar_reserva} (justificativa obrigatória, RF08).
     *
     * @param id            código da reserva
     * @param justificativa motivo
     * @throws SQLException se a reserva não puder ser cancelada
     */
    public void cancelar(int id, String justificativa) throws SQLException {
        try (CallableStatement cs = con.prepareCall("CALL sp_cancelar_reserva(?, ?)")) {
            cs.setInt(1, id);
            cs.setString(2, justificativa);
            cs.execute();
        }
    }
}
