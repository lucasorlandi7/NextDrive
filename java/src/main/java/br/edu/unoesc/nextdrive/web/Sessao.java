package br.edu.unoesc.nextdrive.web;

import br.edu.unoesc.nextdrive.model.Usuario;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Sessão de um navegador: usuário logado, token anti-CSRF e mensagem de aviso da próxima página. */
public final class Sessao {
    private static final Map<String, Sessao> ATIVAS = new ConcurrentHashMap<>();
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final String id;
    private final String csrf;
    private Usuario usuario;
    private String avisoTipo;
    private String avisoTexto;

    private Sessao() {
        this.id = token();
        this.csrf = token();
    }

    private static String token() {
        byte[] b = new byte[24];
        ALEATORIO.nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    /** @return uma sessão nova, já registrada */
    public static Sessao criar() {
        Sessao s = new Sessao();
        ATIVAS.put(s.id, s);
        return s;
    }

    /**
     * @param id valor do cookie de sessão
     * @return a sessão, ou {@code null} se não existir
     */
    public static Sessao buscar(String id) { return id == null ? null : ATIVAS.get(id); }

    /** Encerra a sessão (logout). */
    public void encerrar() { ATIVAS.remove(id); }

    /**
     * Deixa um aviso para aparecer na próxima página exibida.
     *
     * @param tipo "ok" ou "erro"
     * @param texto mensagem
     */
    public void avisar(String tipo, String texto) {
        this.avisoTipo = tipo;
        this.avisoTexto = texto;
    }

    /** @return o aviso pendente como {tipo, texto} e o apaga, ou {@code null} se não houver */
    public String[] consumirAviso() {
        if (avisoTexto == null) return null;
        String[] aviso = {avisoTipo, avisoTexto};
        avisoTipo = null;
        avisoTexto = null;
        return aviso;
    }

    public String getId() { return id; }
    public String getCsrf() { return csrf; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}
