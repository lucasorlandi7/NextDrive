package br.edu.unoesc.nextdrive.web;

import br.edu.unoesc.nextdrive.service.NegocioException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;

/**
 * Servidor web do sistema, sobre o {@code com.sun.net.httpserver} que acompanha o Java (sem
 * frameworks). Cuida das rotas, da sessão, do controle de acesso por perfil (RF02, RN10) e da
 * proteção contra CSRF nos formulários.
 */
public final class Servidor {

    /** Código que trata uma rota. */
    @FunctionalInterface
    public interface Rota {
        Resposta tratar(Requisicao requisicao) throws Exception;
    }

    private static final class Registro {
        final Set<String> perfis;
        final Rota rota;

        Registro(Set<String> perfis, Rota rota) {
            this.perfis = perfis;
            this.rota = rota;
        }
    }

    /** Perfis aceitos por uma rota que exige login de qualquer usuário. */
    public static final String QUALQUER = "*";

    private final Map<String, Registro> rotas = new HashMap<>();
    private final HttpServer http;

    /**
     * @param porta porta TCP onde o servidor vai escutar
     * @throws IOException se a porta estiver ocupada
     */
    public Servidor(int porta) throws IOException {
        http = HttpServer.create(new InetSocketAddress(porta), 0);
        http.createContext("/", this::despachar);
        http.setExecutor(Executors.newFixedThreadPool(8));
    }

    /**
     * Registra uma rota.
     *
     * @param metodo  GET ou POST
     * @param caminho caminho exato, como "/login"
     * @param rota    código que trata a requisição
     * @param perfis  perfis autorizados; vazio = rota pública; {@link #QUALQUER} = qualquer usuário logado
     */
    public void rota(String metodo, String caminho, Rota rota, String... perfis) {
        rotas.put(metodo + " " + caminho, new Registro(perfis.length == 0 ? null : Set.of(perfis), rota));
    }

    /** Inicia o atendimento das requisições. */
    public void iniciar() { http.start(); }

    /**
     * @param s sessão
     * @return valor do cabeçalho Set-Cookie para a sessão
     */
    public static String cookieDeSessao(Sessao s) {
        return "NDSESSAO=" + s.getId() + "; Path=/; HttpOnly; SameSite=Lax";
    }

    private void despachar(HttpExchange troca) throws IOException {
        Resposta resposta;
        Requisicao r = null;
        try {
            r = new Requisicao(troca);
            r.definirSessao(Sessao.buscar(r.cookie("NDSESSAO")));
            resposta = tratar(r);
        } catch (NegocioException e) {
            resposta = paginaErro(r, 400, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            resposta = paginaErro(r, 500, "Ocorreu um erro inesperado. Tente novamente.");
        }
        enviar(troca, resposta);
    }

    private Resposta tratar(Requisicao r) throws Exception {
        Registro reg = rotas.get(r.metodo() + " " + r.caminho());
        if (reg == null) return paginaErro(r, 404, "Página não encontrada.");
        if (reg.perfis != null) {
            if (r.usuario() == null) {
                Sessao s = r.sessao() != null ? r.sessao() : Sessao.criar();
                s.avisar("info", "Entre para continuar.");
                return Resposta.redirecionar("/login").comCookie(cookieDeSessao(s));
            }
            if (!reg.perfis.contains(QUALQUER) && !reg.perfis.contains(r.usuario().getPerfil()))
                return paginaErro(r, 403, "Seu perfil não tem acesso a esta página.");
        }
        if ("POST".equals(r.metodo()) && r.sessao() != null && r.usuario() != null
                && !r.sessao().getCsrf().equals(r.param("_csrf"))) {
            return paginaErro(r, 403, "Formulário expirado. Volte à página e tente de novo.");
        }
        return reg.rota.tratar(r);
    }

    private Resposta paginaErro(Requisicao r, int status, String mensagem) {
        String corpo = "<div class=\"cartao\"><p>" + Pagina.esc(mensagem) + "</p><p><a class=\"botao\" href=\"/\">Voltar ao início</a></p></div>";
        if (r == null) return Resposta.html(status, "<!doctype html><meta charset=\"utf-8\"><p>" + Pagina.esc(mensagem) + "</p>");
        return Resposta.html(status, Pagina.layout(r, status == 404 ? "Não encontrada" : "Não foi possível concluir", corpo));
    }

    private void enviar(HttpExchange troca, Resposta r) throws IOException {
        troca.getResponseHeaders().set("Content-Type", r.getTipo());
        troca.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        troca.getResponseHeaders().set("Cache-Control", "no-store");
        if (r.getLocation() != null) troca.getResponseHeaders().set("Location", r.getLocation());
        if (r.getCookie() != null) troca.getResponseHeaders().add("Set-Cookie", r.getCookie());
        if (r.getNomeArquivo() != null)
            troca.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + r.getNomeArquivo() + "\"");
        byte[] corpo = r.getCorpo();
        troca.sendResponseHeaders(r.getStatus(), corpo.length == 0 ? -1 : corpo.length);
        if (corpo.length > 0) {
            try (OutputStream os = troca.getResponseBody()) { os.write(corpo); }
        }
        troca.close();
    }

    /** @return texto com os perfis, usado em mensagens de log */
    @Override
    public String toString() { return "Servidor" + Arrays.toString(rotas.keySet().toArray()); }
}
