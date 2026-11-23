package br.edu.unoesc.nextdrive.dao;

import br.edu.unoesc.nextdrive.model.Pagamento;
import br.edu.unoesc.nextdrive.model.PagamentoCartao;
import br.edu.unoesc.nextdrive.model.PagamentoPix;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/** Acesso à tabela pagamento. */
public class PagamentoDAO {

    private Connection connection = SingleConnection.getConnection();

    /**
     * Salva um pagamento. Se com ele a locação ficar totalmente paga,
     * a situação da locação passa para PAGA (na mesma transação).
     *
     * @param p pagamento por cartão ou por Pix
     * @return true se salvou
     */
    public boolean salvar(Pagamento p) {
        String sql = "INSERT INTO pagamento (locacao_id, tipo, valor, bandeira, txid) "
                + "VALUES (?, ?, ?, ?, ?)";
        String sqlQuitar = "UPDATE locacao SET status = 'PAGA' WHERE id = ? AND valor_final <= "
                + "(SELECT COALESCE(SUM(valor), 0) FROM pagamento WHERE locacao_id = ?)";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, p.getLocacaoId());
            ps.setString(2, p.getTipo());
            ps.setBigDecimal(3, p.getValor());
            if (p instanceof PagamentoCartao) {
                ps.setString(4, ((PagamentoCartao) p).getBandeira());
                ps.setNull(5, Types.VARCHAR);
            } else {
                ps.setNull(4, Types.VARCHAR);
                ps.setString(5, ((PagamentoPix) p).getTxid());
            }
            ps.execute();
            ps.close();

            ps = connection.prepareStatement(sqlQuitar);
            ps.setInt(1, p.getLocacaoId());
            ps.setInt(2, p.getLocacaoId());
            ps.execute();
            ps.close();

            connection.commit();
            return true;
        } catch (SQLException e) {
            SingleConnection.erro("salvar o pagamento", e);
            return false;
        }
    }

    /**
     * Lista os pagamentos de uma locação.
     *
     * @param locacaoId código da locação
     * @return lista de pagamentos (cartão ou Pix), do mais antigo para o mais novo
     */
    public List<Pagamento> listarPorLocacao(int locacaoId) {
        List<Pagamento> lista = new ArrayList<Pagamento>();
        String sql = "SELECT id, tipo, valor, data_pagamento, bandeira, txid FROM pagamento "
                + "WHERE locacao_id = ? ORDER BY id";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, locacaoId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Pagamento p;
                if (rs.getString("tipo").equals("CARTAO")) {
                    p = new PagamentoCartao(locacaoId, rs.getBigDecimal("valor"), rs.getString("bandeira"));
                } else {
                    p = new PagamentoPix(locacaoId, rs.getBigDecimal("valor"), rs.getString("txid"));
                }
                p.setId(rs.getInt("id"));
                p.setDataPagamento(rs.getTimestamp("data_pagamento").toLocalDateTime());
                lista.add(p);
            }
            rs.close();
            ps.close();
            connection.commit();
        } catch (SQLException e) {
            SingleConnection.erro("listar os pagamentos", e);
        }
        return lista;
    }
}
