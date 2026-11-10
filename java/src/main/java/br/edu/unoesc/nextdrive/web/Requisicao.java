package br.edu.unoesc.nextdrive.web;

import br.edu.unoesc.nextdrive.model.Usuario;
import br.edu.unoesc.nextdrive.service.NegocioException;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

/** Requisição HTTP já interpretada: parâmetros (consulta e formulário), sessão e conversões de tipo. */
public final class Requisicao {
    private final HttpExchange troca;
    private final Map<String, String> params = new HashMap<>();
    private Sessao sessao;
    private String erro;

    Requisicao(HttpExchange troca) throws IOException {
        this.troca = troca;
        interpretar(troca.getRequestURI().getRawQuery());
        if ("POST".equals(troca.getRequestMethod())) {
            interpretar(new String(troca.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    private void interpretar(String texto) {
        if (texto == null || texto.isEmpty()) return;
        for (String par : texto.split("&")) {
            int i = par.indexOf('=');
            String chave = URLDecoder.decode(i < 0 ? par : par.substring(0, i), StandardCharsets.UTF_8);
            String valor = i < 0 ? "" : URLDecoder.decode(par.substring(i + 1), StandardCharsets.UTF_8);
            params.put(chave, valor);
        }
    }

    public String metodo() { return troca.getRequestMethod(); }
    public String caminho() { return troca.getRequestURI().getPath(); }

    String cookie(String nome) {
        String cabecalho = troca.getRequestHeaders().getFirst("Cookie");
        if (cabecalho == null) return null;
        for (String parte : cabecalho.split(";")) {
            String[] kv = parte.trim().split("=", 2);
            if (kv.length == 2 && kv[0].equals(nome)) return kv[1];
        }
        return null;
    }

    void definirSessao(Sessao s) { this.sessao = s; }

    /** @return a sessão atual, ou {@code null} se o visitante não tem sessão */
    public Sessao sessao() { return sessao; }

    /** @return o usuário logado, ou {@code null} */
    public Usuario usuario() { return sessao == null ? null : sessao.getUsuario(); }

    /** @param nome nome do campo @return o texto do campo, sem espaços nas pontas (vazio se ausente) */
    public String param(String nome) { return params.getOrDefault(nome, "").trim(); }

    /** @param nome nome do campo @param padrao valor se ausente @return texto do campo ou o padrão */
    public String param(String nome, String padrao) {
        String v = param(nome);
        return v.isEmpty() ? padrao : v;
    }

    /** @param nome nome do campo @return true se o campo foi enviado e não está vazio */
    public boolean tem(String nome) { return !param(nome).isEmpty(); }

    /**
     * @param nome nome do campo
     * @return o número inteiro do campo
     * @throws NegocioException se estiver vazio ou não for um número
     */
    public int inteiro(String nome) throws NegocioException {
        try {
            return Integer.parseInt(param(nome));
        } catch (NumberFormatException e) {
            throw new NegocioException("Valor inválido no campo \"" + nome.replace('_', ' ') + "\".");
        }
    }

    /**
     * @param nome nome do campo
     * @return o valor decimal (aceita vírgula ou ponto), ou {@code null} se vazio
     * @throws NegocioException se não for um número
     */
    public BigDecimal decimal(String nome) throws NegocioException {
        if (!tem(nome)) return null;
        try {
            String texto = param(nome);
            // "1.234,56" vira 1234.56; "123.45" e "123,45" também são aceitos
            if (texto.contains(",")) texto = texto.replace(".", "").replace(',', '.');
            return new BigDecimal(texto);
        } catch (NumberFormatException e) {
            throw new NegocioException("Valor inválido no campo \"" + nome.replace('_', ' ') + "\".");
        }
    }

    /**
     * @param nome nome do campo (formato do navegador: 2026-11-10T09:00)
     * @return data e hora do campo
     * @throws NegocioException se estiver vazio ou inválido
     */
    public LocalDateTime dataHora(String nome) throws NegocioException {
        try {
            return LocalDateTime.parse(param(nome));
        } catch (DateTimeParseException e) {
            throw new NegocioException("Data e hora inválidas no campo \"" + nome.replace('_', ' ') + "\".");
        }
    }

    /**
     * @param nome nome do campo (formato 2026-11-10)
     * @return data do campo
     * @throws NegocioException se estiver vazio ou inválida
     */
    public LocalDate data(String nome) throws NegocioException {
        try {
            return LocalDate.parse(param(nome));
        } catch (DateTimeParseException e) {
            throw new NegocioException("Data inválida no campo \"" + nome.replace('_', ' ') + "\".");
        }
    }

    /** Define a mensagem de erro exibida no topo da página que será desenhada. */
    public void erro(String mensagem) { this.erro = mensagem; }

    String getErro() { return erro; }
}
