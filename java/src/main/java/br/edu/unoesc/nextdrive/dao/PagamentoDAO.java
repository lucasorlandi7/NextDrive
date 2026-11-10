package br.edu.unoesc.nextdrive.dao;

import br.edu.unoesc.nextdrive.model.Pagamento;
import br.edu.unoesc.nextdrive.model.PagamentoCartao;
import br.edu.unoesc.nextdrive.model.PagamentoPix;
import br.edu.unoesc.nextdrive.util.Tabela;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Acesso aos pagamentos. */
public class PagamentoDAO {
    private final Connection con;

    public PagamentoDAO(Connection con) { this.con = con; }

    /**
     * Registra o pagamento pela procedure {@code sp_registrar_pagamento}, que grava nas tabelas
     * da forma correta, quita a locação quando o total é atingido e libera a cautela.
     *
     * @param p             pagamento já validado (cartão ou PIX)
     * @param funcionarioId quem recebeu
     * @throws SQLException se o pagamento não for aceito (por exemplo, valor acima do saldo)
     */
    public void registrar(Pagamento p, int funcionarioId) throws SQLException {
        String sql = "CALL sp_registrar_pagamento(?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, p.getLocacaoId());
            cs.setBigDecimal(2, p.getValor());
            cs.setString(3, p.getForma().name());
            cs.setInt(4, funcionarioId);
            if (p instanceof PagamentoCartao c) {
                cs.setString(5, c.getBandeira());
                cs.setString(6, c.getFinalCartao());
                cs.setShort(7, (short) c.getParcelas());
                cs.setNull(8, java.sql.Types.VARCHAR);
                cs.setNull(9, java.sql.Types.VARCHAR);
            } else if (p instanceof PagamentoPix px) {
                cs.setNull(5, java.sql.Types.VARCHAR);
                cs.setNull(6, java.sql.Types.VARCHAR);
                cs.setShort(7, (short) 1);
                cs.setString(8, px.getTxid());
                if (px.getChavePix() == null || px.getChavePix().isBlank()) cs.setNull(9, java.sql.Types.VARCHAR);
                else cs.setString(9, px.getChavePix());
            }
            cs.execute();
        }
    }

    /**
     * @param locacaoId código da locação
     * @return colunas: id, forma, valor, data
     * @throws SQLException em erro de banco
     */
    public Tabela porLocacao(int locacaoId) throws SQLException {
        String sql = "SELECT id, forma, valor, data_pagamento AS data FROM pagamento WHERE locacao_id = ? ORDER BY id";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, locacaoId);
            try (ResultSet rs = ps.executeQuery()) {
                return Tabela.de(rs);
            }
        }
    }
}
