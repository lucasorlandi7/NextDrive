package br.edu.unoesc.nextdrive.web;

import java.nio.charset.StandardCharsets;

/** Resposta HTTP pronta para ser enviada pelo {@link Servidor}. */
public final class Resposta {
    private final int status;
    private final String tipo;
    private final byte[] corpo;
    private String location;
    private String cookie;
    private String nomeArquivo;

    private Resposta(int status, String tipo, String corpo) {
        this.status = status;
        this.tipo = tipo;
        this.corpo = corpo.getBytes(StandardCharsets.UTF_8);
    }

    /** @param html página completa @return resposta 200 em HTML */
    public static Resposta html(String html) { return new Resposta(200, "text/html; charset=utf-8", html); }

    /** @param status código HTTP @param html página completa @return resposta em HTML com o status dado */
    public static Resposta html(int status, String html) { return new Resposta(status, "text/html; charset=utf-8", html); }

    /** @param destino caminho para onde o navegador deve ir @return resposta de redirecionamento (303) */
    public static Resposta redirecionar(String destino) {
        Resposta r = new Resposta(303, "text/plain", "");
        r.location = destino;
        return r;
    }

    /**
     * @param csv conteúdo do arquivo
     * @param nomeArquivo nome sugerido para download
     * @return resposta que baixa um arquivo CSV
     */
    public static Resposta csv(String csv, String nomeArquivo) {
        Resposta r = new Resposta(200, "text/csv; charset=utf-8", "﻿" + csv);
        r.nomeArquivo = nomeArquivo;
        return r;
    }

    /** @param valor cabeçalho Set-Cookie completo @return esta resposta */
    public Resposta comCookie(String valor) {
        this.cookie = valor;
        return this;
    }

    public int getStatus() { return status; }
    public String getTipo() { return tipo; }
    public byte[] getCorpo() { return corpo; }
    public String getLocation() { return location; }
    public String getCookie() { return cookie; }
    public String getNomeArquivo() { return nomeArquivo; }
}
