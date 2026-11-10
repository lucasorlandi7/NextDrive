package br.edu.unoesc.nextdrive.web;

import br.edu.unoesc.nextdrive.model.Usuario;
import br.edu.unoesc.nextdrive.util.Tabela;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.function.IntFunction;

/** Montagem das páginas HTML: layout, menu por perfil, tabelas e campos de formulário. */
public final class Pagina {
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Locale BR = Locale.forLanguageTag("pt-BR");

    private static final String CSS = """
        :root{--fundo:#f4f6f9;--cartao:#fff;--texto:#1d2733;--suave:#5b6877;--borda:#d9e0e8;--marca:#0b5ed7;--marca2:#0a4db0;--ok:#146c43;--okbg:#d1e7dd;--erro:#a52834;--errobg:#f8d7da;--aviso:#7a5b00;--avisobg:#fff3cd}
        @media (prefers-color-scheme:dark){:root{--fundo:#12171d;--cartao:#1b232c;--texto:#e6ebf1;--suave:#9aa7b5;--borda:#303b47;--marca:#5b9bff;--marca2:#7eb0ff;--ok:#7fd6a4;--okbg:#14301f;--erro:#ff9aa5;--errobg:#3a1a1e;--aviso:#f1d27a;--avisobg:#3a3012}}
        *{box-sizing:border-box}
        body{margin:0;font:16px/1.5 system-ui,-apple-system,Segoe UI,Roboto,sans-serif;background:var(--fundo);color:var(--texto)}
        header{background:var(--cartao);border-bottom:1px solid var(--borda)}
        .barra{max-width:1100px;margin:0 auto;padding:12px 16px;display:flex;flex-wrap:wrap;gap:6px 18px;align-items:center}
        .marca{font-weight:700;font-size:1.15rem;color:var(--marca);text-decoration:none;margin-right:8px}
        nav{display:flex;flex-wrap:wrap;gap:4px 14px;flex:1}
        nav a{color:var(--texto);text-decoration:none;padding:4px 2px;border-bottom:2px solid transparent}
        nav a:hover{border-color:var(--marca)}
        .quem{color:var(--suave);font-size:.9rem}.quem+a{color:var(--marca)}
        main{max-width:1100px;margin:0 auto;padding:20px 16px 60px}
        h1{font-size:1.6rem;margin:.2em 0 .6em}h2{font-size:1.2rem;margin:0 0 .6em}
        .cartao{background:var(--cartao);border:1px solid var(--borda);border-radius:10px;padding:16px;margin-bottom:18px;overflow-x:auto}
        .grade{display:grid;grid-template-columns:repeat(auto-fit,minmax(240px,1fr));gap:12px}
        table{border-collapse:collapse;width:100%;font-size:.93rem}
        th,td{text-align:left;padding:7px 10px;border-bottom:1px solid var(--borda);white-space:nowrap}
        th{color:var(--suave);font-weight:600}
        label{display:block;font-size:.88rem;color:var(--suave);margin:10px 0 3px}
        input,select,textarea{width:100%;padding:8px 10px;border:1px solid var(--borda);border-radius:7px;background:var(--cartao);color:var(--texto);font:inherit}
        button,.botao{display:inline-block;background:var(--marca);color:#fff;border:0;border-radius:7px;padding:8px 16px;font:inherit;cursor:pointer;text-decoration:none}
        button:hover,.botao:hover{background:var(--marca2)}
        .sec{background:transparent;color:var(--marca);border:1px solid var(--marca)}
        .sec:hover{background:var(--marca);color:#fff}
        .pequeno{padding:3px 10px;font-size:.85rem}
        .aviso{padding:10px 14px;border-radius:8px;margin-bottom:16px}
        .aviso.ok{background:var(--okbg);color:var(--ok)}.aviso.erro{background:var(--errobg);color:var(--erro)}.aviso.info{background:var(--avisobg);color:var(--aviso)}
        .b{display:inline-block;padding:1px 9px;border-radius:99px;font-size:.8rem;background:var(--borda)}
        .b-ATIVA,.b-DISPONIVEL,.b-PAGA,.b-LIBERADA,.b-CONCLUIDA{background:var(--okbg);color:var(--ok)}
        .b-CANCELADA,.b-PENDENTE,.b-INATIVO{background:var(--errobg);color:var(--erro)}
        .b-ABERTA,.b-LOCADO,.b-RETIDA,.b-EM_ANDAMENTO,.b-MANUTENCAO,.b-ENCERRADA{background:var(--avisobg);color:var(--aviso)}
        .duas{display:grid;grid-template-columns:repeat(auto-fit,minmax(200px,1fr));gap:0 14px}
        .dados{display:grid;grid-template-columns:max-content 1fr;gap:4px 16px;margin:0}.dados dt{color:var(--suave)}.dados dd{margin:0}
        .barra-graf{height:18px;background:var(--marca);border-radius:4px;min-width:2px}
        .suave{color:var(--suave)}.linha{display:flex;gap:8px;flex-wrap:wrap;align-items:center}
        form.em-linha{display:inline-flex;gap:6px;align-items:center}form.em-linha input{width:auto;min-width:120px;padding:3px 8px}
        """;

    private static final Map<String, String> PALAVRAS = Map.ofEntries(
            Map.entry("devolucao", "devolução"), Map.entry("locacao", "locação"), Map.entry("locacoes", "locações"),
            Map.entry("veiculo", "veículo"), Map.entry("descricao", "descrição"), Map.entry("infracao", "infração"),
            Map.entry("mes", "mês"), Map.entry("credito", "crédito"), Map.entry("debito", "débito"),
            Map.entry("manutencao", "manutenção"), Map.entry("diaria", "diária"), Map.entry("diarias", "diárias"),
            Map.entry("estimativa", "estimativa"), Map.entry("categoria", "categoria"), Map.entry("acoes", "ações"));

    private static final Map<String, String> SITUACOES = Map.of(
            "DISPONIVEL", "Disponível", "MANUTENCAO", "Manutenção", "EM_ANDAMENTO", "Em andamento",
            "CONCLUIDA", "Concluída", "ENCERRADA", "Encerrada", "CANCELADA", "Cancelada", "EFETIVADA", "Efetivada");

    private Pagina() { }

    /** Transforma o nome de uma coluna do banco em um título legível, com acentos. */
    static String cabecalho(String coluna) {
        StringBuilder sb = new StringBuilder();
        for (String palavra : coluna.toLowerCase().split("_")) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(PALAVRAS.getOrDefault(palavra, palavra));
        }
        return sb.length() == 0 ? "" : Character.toUpperCase(sb.charAt(0)) + sb.substring(1);
    }

    /** Escapa texto para HTML (evita injeção de script, RNF04). */
    public static String esc(Object valor) {
        if (valor == null) return "";
        return valor.toString().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    /** Formata valor em reais, como "R$ 1.234,56". */
    public static String moeda(Object valor) {
        if (valor == null) return "–";
        BigDecimal v = valor instanceof BigDecimal b ? b : new BigDecimal(valor.toString());
        return NumberFormat.getCurrencyInstance(BR).format(v);
    }

    /** Formata uma célula de tabela conforme o tipo do valor. */
    public static String formatar(Object v, String coluna) {
        if (v == null) return "<span class=\"suave\">–</span>";
        if (v instanceof BigDecimal b) return esc(moeda(b));
        if (v instanceof Timestamp t) return esc(t.toLocalDateTime().format(DATA_HORA));
        if (v instanceof java.sql.Date d) return esc(d.toLocalDate().format(DATA));
        if (v instanceof LocalDateTime t) return esc(t.format(DATA_HORA));
        if (v instanceof LocalDate d) return esc(d.format(DATA));
        if (v instanceof Boolean b) return b ? "Sim" : "Não";
        String texto = v.toString();
        if (coluna.equals("status") || coluna.equals("cautela"))
            return "<span class=\"b b-" + esc(texto) + "\">" + esc(SITUACOES.getOrDefault(texto, texto)) + "</span>";
        return esc(texto);
    }

    /** Monta a página completa: cabeçalho, menu do perfil, avisos e o corpo. */
    public static String layout(Requisicao r, String titulo, String corpo) {
        Usuario u = r.usuario();
        StringBuilder menu = new StringBuilder();
        if (u == null) {
            link(menu, "/frota", "Disponibilidade");
            link(menu, "/cadastro", "Cadastre-se");
            link(menu, "/login", "Entrar");
        } else if (u.isGerente()) {
            link(menu, "/gerente", "Painel gerencial");
            link(menu, "/gerente/frota", "Frota");
            link(menu, "/gerente/categorias", "Categorias e preços");
            link(menu, "/gerente/manutencao", "Manutenção");
            link(menu, "/gerente/funcionarios", "Funcionários");
            link(menu, "/gerente/auditoria", "Auditoria");
            link(menu, "/atendente", "Balcão");
        } else if (u.isFuncionario()) {
            link(menu, "/atendente", "Painel do balcão");
            link(menu, "/reserva/nova", "Nova reserva");
            link(menu, "/atendente/clientes", "Clientes");
            link(menu, "/atendente/cliente/novo", "Novo cliente");
            link(menu, "/atendente/multa", "Multas");
            link(menu, "/frota", "Disponibilidade");
        } else {
            link(menu, "/cliente", "Minha área");
            link(menu, "/reserva/nova", "Nova reserva");
            link(menu, "/frota", "Disponibilidade");
        }
        String quem = u == null ? "" : "<span class=\"quem\">" + esc(u.getNome()) + " (" + esc(u.getPerfil().toLowerCase()) + ")</span> <a href=\"/sair\">Sair</a>";

        StringBuilder aviso = new StringBuilder();
        if (r.getErro() != null) aviso.append("<div class=\"aviso erro\" role=\"alert\">").append(esc(r.getErro())).append("</div>");
        if (r.sessao() != null) {
            String[] a = r.sessao().consumirAviso();
            if (a != null) aviso.append("<div class=\"aviso ").append(esc(a[0])).append("\" role=\"status\">").append(esc(a[1])).append("</div>");
        }
        return "<!doctype html><html lang=\"pt-BR\"><head><meta charset=\"utf-8\">"
             + "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">"
             + "<title>" + esc(titulo) + " - Next Drive</title><style>" + CSS + "</style></head><body>"
             + "<header><div class=\"barra\"><a class=\"marca\" href=\"/\">Next Drive</a><nav>" + menu + "</nav>" + quem + "</div></header>"
             + "<main><h1>" + esc(titulo) + "</h1>" + aviso + corpo + "</main></body></html>";
    }

    private static void link(StringBuilder sb, String url, String texto) {
        sb.append("<a href=\"").append(url).append("\">").append(esc(texto)).append("</a>");
    }

    /** Envolve conteúdo em um cartão com título opcional. */
    public static String cartao(String titulo, String conteudo) {
        return "<section class=\"cartao\">" + (titulo == null ? "" : "<h2>" + esc(titulo) + "</h2>") + conteudo + "</section>";
    }

    /** Tabela somente leitura. */
    public static String tabela(Tabela t) { return tabela(t, null, null); }

    /**
     * Tabela com uma coluna extra de ações.
     *
     * @param t      dados
     * @param titulo cabeçalho da coluna de ações (null para não ter a coluna)
     * @param acao   recebe o índice da linha e devolve o HTML dos botões
     */
    public static String tabela(Tabela t, String titulo, IntFunction<String> acao) {
        if (t.isVazia()) return "<p class=\"suave\">Nenhum registro.</p>";
        StringBuilder sb = new StringBuilder("<table><thead><tr>");
        for (String c : t.getColunas()) sb.append("<th>").append(esc(cabecalho(c))).append("</th>");
        if (titulo != null) sb.append("<th>").append(esc(titulo)).append("</th>");
        sb.append("</tr></thead><tbody>");
        for (int i = 0; i < t.getLinhas().size(); i++) {
            sb.append("<tr>");
            List<Object> linha = t.getLinhas().get(i);
            for (int j = 0; j < linha.size(); j++)
                sb.append("<td>").append(formatar(linha.get(j), t.getColunas().get(j))).append("</td>");
            if (titulo != null) sb.append("<td>").append(acao.apply(i)).append("</td>");
            sb.append("</tr>");
        }
        return sb.append("</tbody></table>").toString();
    }

    /** Formulário POST com o token anti-CSRF da sessão. */
    public static String form(Requisicao r, String action, String conteudo) {
        String token = r.sessao() == null ? "" : "<input type=\"hidden\" name=\"_csrf\" value=\"" + esc(r.sessao().getCsrf()) + "\">";
        return "<form method=\"post\" action=\"" + esc(action) + "\">" + token + conteudo + "</form>";
    }

    /** Formulário pequeno, em linha, para botões dentro de tabelas. */
    public static String formEmLinha(Requisicao r, String action, String conteudo) {
        return form(r, action, conteudo).replace("<form ", "<form class=\"em-linha\" ");
    }

    /** Campo de texto, número ou data; o valor vem do que foi enviado, ou do padrão. */
    public static String campo(Requisicao r, String rotulo, String nome, String tipo, String padrao, String extra) {
        String valor = r.tem(nome) ? r.param(nome) : (padrao == null ? "" : padrao);
        return "<label for=\"" + nome + "\">" + esc(rotulo) + "</label><input id=\"" + nome + "\" name=\"" + nome
             + "\" type=\"" + tipo + "\" value=\"" + esc(valor) + "\" " + (extra == null ? "" : extra) + ">";
    }

    /**
     * Lista de seleção.
     *
     * @param opcoes pares {valor, rótulo}
     */
    public static String select(Requisicao r, String rotulo, String nome, List<String[]> opcoes, boolean obrigatorio) {
        String atual = r.param(nome);
        StringBuilder sb = new StringBuilder("<label for=\"" + nome + "\">" + esc(rotulo) + "</label><select id=\""
                + nome + "\" name=\"" + nome + "\"" + (obrigatorio ? " required" : "") + ">");
        if (!obrigatorio) sb.append("<option value=\"\">–</option>");
        for (String[] o : opcoes)
            sb.append("<option value=\"").append(esc(o[0])).append("\"").append(o[0].equals(atual) ? " selected" : "")
              .append(">").append(esc(o[1])).append("</option>");
        return sb.append("</select>").toString();
    }

    /** Lista de pares rótulo/valor (descrição). */
    public static String dados(Object... paresRotuloValor) {
        StringBuilder sb = new StringBuilder("<dl class=\"dados\">");
        for (int i = 0; i + 1 < paresRotuloValor.length; i += 2)
            sb.append("<dt>").append(esc(paresRotuloValor[i])).append("</dt><dd>").append(paresRotuloValor[i + 1]).append("</dd>");
        return sb.append("</dl>").toString();
    }

    /** Formata uma data e hora para exibição. */
    public static String dataHora(LocalDateTime d) { return d == null ? "–" : d.format(DATA_HORA); }

    /** Valor da data e hora no formato do campo datetime-local. */
    public static String paraCampo(LocalDateTime d) { return d.withSecond(0).withNano(0).toString(); }
}
