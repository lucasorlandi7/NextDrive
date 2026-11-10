package br.edu.unoesc.nextdrive.service;

import br.edu.unoesc.nextdrive.model.Usuario;
import br.edu.unoesc.nextdrive.util.Conexao;
import br.edu.unoesc.nextdrive.util.Conexao.Papel;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Base das classes de serviço: abre uma conexão com o papel de banco adequado, executa a operação
 * e traduz erros do banco em {@link NegocioException}.
 */
public abstract class Servico {
    private final Papel papel;

    /** Operação que usa uma conexão aberta. */
    @FunctionalInterface
    protected interface Operacao<T> {
        T executar(Connection con) throws SQLException, NegocioException;
    }

    protected Servico(Papel papel) { this.papel = papel; }

    /**
     * Escolhe o papel de banco de um usuário: gerente usa o papel de gerente; clientes e
     * atendentes usam o papel de atendente (o que o cliente enxerga é filtrado pela aplicação).
     *
     * @param u usuário da sessão
     * @return papel de banco correspondente
     */
    public static Papel papelDe(Usuario u) { return u.isGerente() ? Papel.GERENTE : Papel.ATENDENTE; }

    /**
     * Executa a operação em uma conexão própria, fechada ao final.
     *
     * @param op operação a executar
     * @param <T> tipo do resultado
     * @return o resultado da operação
     * @throws NegocioException se a regra de negócio ou o banco recusar a operação
     */
    protected <T> T executar(Operacao<T> op) throws NegocioException {
        try (Connection con = Conexao.abrir(papel)) {
            return op.executar(con);
        } catch (SQLException e) {
            throw NegocioException.de(e);
        }
    }

    /** Executa uma operação que não devolve valor. */
    protected void executarSemRetorno(Operacao<Void> op) throws NegocioException { executar(op); }
}
