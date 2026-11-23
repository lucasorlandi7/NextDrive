package br.edu.unoesc.nextdrive.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Conexão única com o banco PostgreSQL, usada por todos os DAOs.
 * Se a sua senha ou a porta forem diferentes, troque nas variáveis abaixo.
 */
public class SingleConnection {

    private static String url = "jdbc:postgresql://localhost:5432/nextdrive";
    private static String user = "postgres";
    private static String password = "POSTEGRE";
    private static Connection connection = null;

    static {
        conectar();
    }

    private static void conectar() {
        try {
            if (connection == null) {
                Class.forName("org.postgresql.Driver");
                connection = DriverManager.getConnection(url, user, password);
                connection.setAutoCommit(false);
            }
        } catch (Exception e) {
            System.out.println("Erro ao conectar no banco: " + e.getMessage());
        }
    }

    /**
     * Devolve a conexão aberta.
     *
     * @return a conexão, ou null se não foi possível conectar
     */
    public static Connection getConnection() {
        return connection;
    }

    /**
     * Mostra o erro na tela e desfaz (rollback) o que estava pendente na transação.
     *
     * @param acao o que estava sendo feito (por exemplo, "salvar o cliente")
     * @param e    o erro ocorrido
     */
    public static void erro(String acao, SQLException e) {
        System.out.println("Erro ao " + acao + ": " + e.getMessage());
        try {
            connection.rollback();
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
    }
}
