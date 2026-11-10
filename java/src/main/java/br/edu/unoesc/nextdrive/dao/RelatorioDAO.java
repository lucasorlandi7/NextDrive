package br.edu.unoesc.nextdrive.dao;

import br.edu.unoesc.nextdrive.util.Tabela;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Consultas dos relatórios administrativos (RF14), baseadas nas views do banco. */
public class RelatorioDAO {
    private final Connection con;

    public RelatorioDAO(Connection con) { this.con = con; }

    /**
     * @return faturamento por mês e forma de pagamento ({@code vw_faturamento_mensal})
     * @throws SQLException em erro de banco
     */
    public Tabela faturamentoMensal() throws SQLException {
        return consultar("SELECT mes, qtd_pagamentos AS pagamentos, total_recebido AS total, "
                       + "COALESCE(total_credito, 0) AS credito, COALESCE(total_debito, 0) AS debito, "
                       + "COALESCE(total_pix, 0) AS pix FROM vw_faturamento_mensal ORDER BY mes");
    }

    /**
     * @return os últimos registros da trilha de auditoria (RF17)
     * @throws SQLException em erro de banco
     */
    public Tabela auditoria() throws SQLException {
        return consultar("SELECT id, data_hora AS quando, tabela, operacao AS op, registro_id AS registro, "
                       + "usuario_db AS usuario, COALESCE(dados_novos::text, '') AS dados_novos "
                       + "FROM auditoria ORDER BY id DESC LIMIT 50");
    }

    private Tabela consultar(String sql) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return Tabela.de(rs);
        }
    }
}
