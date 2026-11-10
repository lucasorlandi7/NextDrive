package br.edu.unoesc.nextdrive.dao;

import br.edu.unoesc.nextdrive.model.Locacao;
import br.edu.unoesc.nextdrive.model.StatusLocacao;
import br.edu.unoesc.nextdrive.util.Tabela;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Optional;

/** Acesso às locações. As operações que mudam dados chamam as procedures do banco. */
public class LocacaoDAO {
    private final Connection con;

    public LocacaoDAO(Connection con) { this.con = con; }

    /**
     * Abre a locação ({@code sp_abrir_locacao}): valida condutor e conflitos, marca o veículo como
     * locado e efetiva a reserva.
     *
     * @throws SQLException com a regra violada, se a retirada não for permitida
     */
    public void abrir(int reservaId, int veiculoId, int condutorId, int funcionarioId, int kmSaida,
                      int combustivelSaida) throws SQLException {
        try (CallableStatement cs = con.prepareCall("CALL sp_abrir_locacao(?, ?, ?, ?, ?, ?)")) {
            cs.setInt(1, reservaId);
            cs.setInt(2, veiculoId);
            cs.setInt(3, condutorId);
            cs.setInt(4, funcionarioId);
            cs.setInt(5, kmSaida);
            cs.setShort(6, (short) combustivelSaida);
            cs.execute();
        }
    }

    /**
     * Registra a devolução ({@code sp_registrar_devolucao}): o banco calcula diárias, horas extras,
     * combustível, taxa de outra loja e valor final.
     *
     * @throws SQLException com a regra violada, se a devolução não for permitida
     */
    public void devolver(int locacaoId, int kmChegada, int combustivelChegada, int lojaDevolucaoId,
                         int funcionarioId, BigDecimal desconto) throws SQLException {
        try (CallableStatement cs = con.prepareCall("CALL sp_registrar_devolucao(?, ?, ?, ?, ?, ?)")) {
            cs.setInt(1, locacaoId);
            cs.setInt(2, kmChegada);
            cs.setShort(3, (short) combustivelChegada);
            cs.setInt(4, lojaDevolucaoId);
            cs.setInt(5, funcionarioId);
            if (desconto == null) cs.setNull(6, Types.NUMERIC);
            else cs.setBigDecimal(6, desconto);
            cs.execute();
        }
    }

    /**
     * @param id código da locação
     * @return a locação com o total já pago, se existir
     * @throws SQLException em erro de banco
     */
    public Optional<Locacao> buscar(int id) throws SQLException {
        String sql = "SELECT lo.id, lo.reserva_id, lo.veiculo_id, lo.condutor_id, lo.data_retirada, "
                   + "lo.data_devolucao_prevista, lo.data_devolucao_real, lo.km_saida, lo.km_chegada, "
                   + "lo.combustivel_saida, lo.combustivel_chegada, lo.cautela_valor, lo.cautela_status, "
                   + "lo.valor_diaria_contratada, lo.valor_diarias, lo.valor_horas_extras, lo.valor_combustivel, "
                   + "lo.taxa_devolucao_outra_loja, lo.desconto, lo.valor_final, lo.status, "
                   + "COALESCE((SELECT SUM(valor) FROM pagamento WHERE locacao_id = lo.id), 0) "
                   + "FROM locacao lo WHERE lo.id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                Locacao l = new Locacao();
                l.setId(rs.getInt(1));
                l.setReservaId(rs.getInt(2));
                l.setVeiculoId(rs.getInt(3));
                l.setCondutorId(rs.getInt(4));
                l.setDataRetirada(rs.getTimestamp(5).toLocalDateTime());
                l.setDataDevolucaoPrevista(rs.getTimestamp(6).toLocalDateTime());
                if (rs.getTimestamp(7) != null) l.setDataDevolucaoReal(rs.getTimestamp(7).toLocalDateTime());
                l.setKmSaida(rs.getInt(8));
                int kmChegada = rs.getInt(9);
                if (!rs.wasNull()) l.setKmChegada(kmChegada);
                l.setCombustivelSaida(rs.getInt(10));
                int combChegada = rs.getInt(11);
                if (!rs.wasNull()) l.setCombustivelChegada(combChegada);
                l.setCautelaValor(rs.getBigDecimal(12));
                l.setCautelaLiberada("LIBERADA".equals(rs.getString(13)));
                l.setValorDiariaContratada(rs.getBigDecimal(14));
                l.setValorDiarias(rs.getBigDecimal(15));
                l.setValorHorasExtras(rs.getBigDecimal(16));
                l.setValorCombustivel(rs.getBigDecimal(17));
                l.setTaxaDevolucaoOutraLoja(rs.getBigDecimal(18));
                l.setDesconto(rs.getBigDecimal(19));
                l.setValorFinal(rs.getBigDecimal(20));
                l.setStatus(StatusLocacao.valueOf(rs.getString(21)));
                l.setTotalPago(rs.getBigDecimal(22));
                return Optional.of(l);
            }
        }
    }

    /**
     * Locações abertas (veículos na rua).
     *
     * @return colunas: id, cliente, condutor, placa, veiculo, retirada, devolucao_prevista
     * @throws SQLException em erro de banco
     */
    public Tabela abertas() throws SQLException {
        String sql = "SELECT locacao_id AS id, cliente, condutor, placa, veiculo, data_retirada AS retirada, "
                   + "data_devolucao_prevista AS devolucao_prevista FROM vw_locacoes_detalhadas "
                   + "WHERE status = 'ABERTA' ORDER BY data_devolucao_prevista";
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return Tabela.de(rs);
        }
    }

    /**
     * Locações encerradas com saldo a pagar.
     *
     * @return colunas: id, cliente, valor_final, total_pago, saldo_devedor
     * @throws SQLException em erro de banco
     */
    public Tabela aguardandoPagamento() throws SQLException {
        String sql = "SELECT locacao_id AS id, cliente, valor_final, total_pago, saldo_devedor "
                   + "FROM vw_saldo_locacao WHERE status = 'ENCERRADA' AND saldo_devedor > 0 ORDER BY locacao_id";
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return Tabela.de(rs);
        }
    }

    /**
     * Resumo de uma locação para exibir nas telas de devolução, pagamento e multa.
     *
     * @param id código da locação
     * @return uma linha da view {@code vw_locacoes_detalhadas}
     * @throws SQLException em erro de banco
     */
    public Tabela detalhe(int id) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT * FROM vw_locacoes_detalhadas WHERE locacao_id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return Tabela.de(rs);
            }
        }
    }

    /**
     * @param id código da locação
     * @return código da loja de retirada e categoria do veículo, na ordem [loja, categoria], se existir
     * @throws SQLException em erro de banco
     */
    public Optional<int[]> lojaECategoria(int id) throws SQLException {
        String sql = "SELECT lo.loja_retirada_id, v.categoria_id FROM locacao lo JOIN veiculo v ON v.id = lo.veiculo_id WHERE lo.id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(new int[]{rs.getInt(1), rs.getInt(2)}) : Optional.empty();
            }
        }
    }
}
