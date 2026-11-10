package br.edu.unoesc.nextdrive.service;

import java.sql.SQLException;

/**
 * Erro de regra de negócio com mensagem pronta para o usuário. Pode vir de uma validação em Java
 * ou de uma regra aplicada pelo banco (trigger, procedure ou constraint).
 */
public class NegocioException extends Exception {
    private static final long serialVersionUID = 1L;

    public NegocioException(String mensagem) { super(mensagem); }

    public NegocioException(String mensagem, Throwable causa) { super(mensagem, causa); }

    /**
     * Traduz uma {@link SQLException} em mensagem amigável. Mensagens levantadas pelas regras do
     * banco ({@code RAISE EXCEPTION}) são repassadas como estão; violações de constraint viram
     * textos genéricos.
     *
     * @param e exceção do JDBC
     * @return exceção de negócio equivalente
     */
    public static NegocioException de(SQLException e) {
        String estado = e.getSQLState() == null ? "" : e.getSQLState();
        String msg = e.getMessage() == null ? "" : e.getMessage();
        if (e instanceof org.postgresql.util.PSQLException p && p.getServerErrorMessage() != null
                && p.getServerErrorMessage().getMessage() != null) {
            msg = p.getServerErrorMessage().getMessage();
        }
        switch (estado) {
            case "23505": return new NegocioException("Já existe um registro com esses dados (e-mail, CPF, CNPJ, placa ou CNH repetidos).", e);
            case "23503": return new NegocioException("Registro relacionado não encontrado.", e);
            case "23514": return new NegocioException("Algum valor informado não é permitido: " + msg, e);
            case "23502": return new NegocioException("Preencha todos os campos obrigatórios.", e);
            case "42501": return new NegocioException("Seu perfil não tem permissão para esta operação.", e);
            case "P0001": return new NegocioException(msg, e);
            default:
                if (estado.startsWith("08")) return new NegocioException("Não foi possível conectar ao banco de dados.", e);
                return new NegocioException("Erro no banco de dados: " + msg, e);
        }
    }
}
