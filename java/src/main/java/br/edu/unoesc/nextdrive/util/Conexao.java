package br.edu.unoesc.nextdrive.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Fábrica de conexões JDBC com o PostgreSQL. Cada papel da aplicação usa o usuário de banco
 * correspondente criado em {@code sql/07_roles_privilegios.sql}, de modo que o banco também
 * limita o que cada perfil pode fazer (política de acesso, BDII).
 *
 * <p>Tudo é configurável por variáveis de ambiente: {@code NEXTDRIVE_DB_URL},
 * {@code NEXTDRIVE_DB_USER_<PAPEL>} e {@code NEXTDRIVE_DB_SENHA_<PAPEL>}.</p>
 */
public final class Conexao {

    /** Papéis de banco usados pela aplicação. */
    public enum Papel {
        /** Visitante: login e autocadastro. */
        SITE("usr_site"),
        /** Atendente (também usado nas consultas do próprio cliente logado). */
        ATENDENTE("usr_atendente"),
        /** Gerente: cadastros, manutenção e relatórios. */
        GERENTE("usr_gerente");

        private final String usuarioPadrao;

        Papel(String usuarioPadrao) { this.usuarioPadrao = usuarioPadrao; }
    }

    private static final String URL =
            env("NEXTDRIVE_DB_URL", "jdbc:postgresql://localhost:5432/nextdrive");

    private Conexao() { }

    /**
     * Abre uma conexão nova. Quem chama deve fechá-la (try-with-resources).
     *
     * @param papel papel de banco desejado
     * @return conexão aberta com auto-commit ligado
     * @throws SQLException se o banco não responder ou as credenciais forem recusadas
     */
    public static Connection abrir(Papel papel) throws SQLException {
        String usuario = env("NEXTDRIVE_DB_USER_" + papel.name(), papel.usuarioPadrao);
        String senha = env("NEXTDRIVE_DB_SENHA_" + papel.name(), "Trocar@123");
        return DriverManager.getConnection(URL, usuario, senha);
    }

    /** @return a URL JDBC em uso (exibida na inicialização) */
    public static String getUrl() { return URL; }

    private static String env(String nome, String padrao) {
        String valor = System.getenv(nome);
        return valor == null || valor.isBlank() ? padrao : valor;
    }
}
