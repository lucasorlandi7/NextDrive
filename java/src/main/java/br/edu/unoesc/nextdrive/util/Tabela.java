package br.edu.unoesc.nextdrive.util;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Resultado tabular somente de leitura (cabeçalhos e linhas), usado em listagens e relatórios
 * que não precisam virar objetos de domínio.
 */
public final class Tabela {
    private final List<String> colunas;
    private final List<List<Object>> linhas;

    private Tabela(List<String> colunas, List<List<Object>> linhas) {
        this.colunas = colunas;
        this.linhas = linhas;
    }

    /**
     * Lê todo o {@link ResultSet} para a memória.
     *
     * @param rs resultado da consulta
     * @return tabela com os nomes (labels) das colunas e os valores
     * @throws SQLException em erro de leitura
     */
    public static Tabela de(ResultSet rs) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        List<String> cols = new ArrayList<>();
        for (int i = 1; i <= md.getColumnCount(); i++) cols.add(md.getColumnLabel(i));
        List<List<Object>> dados = new ArrayList<>();
        while (rs.next()) {
            List<Object> linha = new ArrayList<>();
            for (int i = 1; i <= cols.size(); i++) linha.add(rs.getObject(i));
            dados.add(linha);
        }
        return new Tabela(cols, dados);
    }

    public List<String> getColunas() { return colunas; }
    public List<List<Object>> getLinhas() { return linhas; }
    public boolean isVazia() { return linhas.isEmpty(); }

    /**
     * @param linha índice da linha
     * @param coluna nome da coluna
     * @return o valor da célula
     */
    public Object valor(int linha, String coluna) { return linhas.get(linha).get(colunas.indexOf(coluna)); }

    /** @return o texto da célula (vazio se nula) */
    public String texto(int linha, String coluna) {
        Object v = valor(linha, coluna);
        return v == null ? "" : v.toString();
    }
}
