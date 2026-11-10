package br.edu.unoesc.nextdrive.web;

import br.edu.unoesc.nextdrive.model.CategoriaVeiculo;
import br.edu.unoesc.nextdrive.model.Cargo;
import br.edu.unoesc.nextdrive.model.Funcionario;
import br.edu.unoesc.nextdrive.model.Loja;
import br.edu.unoesc.nextdrive.model.Manutencao;
import br.edu.unoesc.nextdrive.model.StatusVeiculo;
import br.edu.unoesc.nextdrive.model.TipoManutencao;
import br.edu.unoesc.nextdrive.model.Veiculo;
import br.edu.unoesc.nextdrive.service.GerenciaService;
import br.edu.unoesc.nextdrive.service.LocacaoService;
import br.edu.unoesc.nextdrive.service.NegocioException;
import br.edu.unoesc.nextdrive.util.Tabela;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/** Telas do gerente: relatórios (RF14), frota, preços, manutenção, funcionários e auditoria. */
public class GerenteControlador {
    private static final String GERENTE = "GERENTE";

    /** Registra as rotas deste controlador. */
    public void registrar(Servidor s) {
        s.rota("GET", "/gerente", r -> painel(r), GERENTE);
        s.rota("GET", "/gerente/exportar", this::exportar, GERENTE);
        s.rota("GET", "/gerente/frota", r -> telaFrota(r), GERENTE);
        s.rota("POST", "/gerente/frota", this::cadastrarVeiculo, GERENTE);
        s.rota("POST", "/gerente/frota/status", this::statusVeiculo, GERENTE);
        s.rota("GET", "/gerente/categorias", r -> telaCategorias(r), GERENTE);
        s.rota("POST", "/gerente/categorias", this::salvarCategoria, GERENTE);
        s.rota("GET", "/gerente/manutencao", r -> telaManutencao(r), GERENTE);
        s.rota("POST", "/gerente/manutencao", this::abrirManutencao, GERENTE);
        s.rota("POST", "/gerente/manutencao/concluir", this::concluirManutencao, GERENTE);
        s.rota("GET", "/gerente/funcionarios", r -> telaFuncionarios(r), GERENTE);
        s.rota("POST", "/gerente/funcionarios", this::cadastrarFuncionario, GERENTE);
        s.rota("POST", "/gerente/funcionarios/ativo", this::ativoFuncionario, GERENTE);
        s.rota("GET", "/gerente/auditoria", r -> Resposta.html(Pagina.layout(r, "Auditoria",
                Pagina.cartao("Últimas 50 operações auditadas (pagamentos e alterações de preço)",
                        Pagina.tabela(new GerenciaService(r.usuario()).auditoria())))), GERENTE);
    }

    // --------------------------------------------------------------- relatórios

    private Resposta painel(Requisicao r) throws Exception {
        GerenciaService g = new GerenciaService(r.usuario());
        Tabela fat = g.faturamentoMensal();
        BigDecimal maior = BigDecimal.ONE;
        for (int i = 0; i < fat.getLinhas().size(); i++) {
            BigDecimal t = (BigDecimal) fat.valor(i, "total");
            if (t.compareTo(maior) > 0) maior = t;
        }
        StringBuilder grafico = new StringBuilder("<table><tbody>");
        for (int i = 0; i < fat.getLinhas().size(); i++) {
            BigDecimal t = (BigDecimal) fat.valor(i, "total");
            long pct = t.multiply(BigDecimal.valueOf(100)).divide(maior, 0, java.math.RoundingMode.HALF_UP).longValue();
            grafico.append("<tr><td>").append(Pagina.esc(fat.texto(i, "mes"))).append("</td><td style=\"width:100%\">")
                   .append("<div class=\"barra-graf\" style=\"width:").append(Math.max(pct, 1)).append("%\"></div></td><td>")
                   .append(Pagina.moeda(t)).append("</td></tr>");
        }
        grafico.append("</tbody></table>");

        String corpo = "<p class=\"linha\"><a class=\"botao sec\" href=\"/gerente/exportar?rel=faturamento\">Exportar faturamento (CSV)</a>"
                + "<a class=\"botao sec\" href=\"/gerente/exportar?rel=frota\">Exportar frota (CSV)</a>"
                + "<a class=\"botao sec\" href=\"/gerente/exportar?rel=multas\">Exportar multas (CSV)</a></p>"
                + Pagina.cartao("Faturamento mensal", (fat.isVazia() ? "" : grafico.toString()) + Pagina.tabela(fat))
                + Pagina.cartao("Utilização da frota", Pagina.tabela(g.frota()))
                + Pagina.cartao("Multas pendentes", Pagina.tabela(new LocacaoService(r.usuario()).multasPendentes()));
        return Resposta.html(Pagina.layout(r, "Painel gerencial", corpo));
    }

    private Resposta exportar(Requisicao r) throws Exception {
        GerenciaService g = new GerenciaService(r.usuario());
        String rel = r.param("rel");
        Tabela t = switch (rel) {
            case "faturamento" -> g.faturamentoMensal();
            case "frota" -> g.frota();
            case "multas" -> new LocacaoService(r.usuario()).multasPendentes();
            default -> throw new NegocioException("Relatório desconhecido.");
        };
        StringBuilder csv = new StringBuilder(String.join(";", t.getColunas())).append("\r\n");
        for (List<Object> linha : t.getLinhas()) {
            List<String> celulas = new ArrayList<>();
            for (Object v : linha) {
                String texto = v == null ? "" : v instanceof BigDecimal b ? b.toPlainString().replace('.', ',')
                        : v instanceof Timestamp ts ? ts.toLocalDateTime().toString().replace('T', ' ') : v.toString();
                celulas.add("\"" + texto.replace("\"", "\"\"") + "\"");
            }
            csv.append(String.join(";", celulas)).append("\r\n");
        }
        return Resposta.csv(csv.toString(), "nextdrive-" + rel + ".csv");
    }

    // -------------------------------------------------------------------- frota

    private Resposta telaFrota(Requisicao r) throws Exception {
        GerenciaService g = new GerenciaService(r.usuario());
        Tabela frota = g.frota();
        List<String[]> lojas = new ArrayList<>();
        for (Loja l : g.lojas()) lojas.add(new String[]{String.valueOf(l.getId()), l.getRotulo()});
        List<String[]> cats = new ArrayList<>();
        for (CategoriaVeiculo c : g.categorias()) cats.add(new String[]{String.valueOf(c.getId()), c.getNome()});
        String tabela = Pagina.tabela(frota, "Ações", i -> {
            String st = frota.texto(i, "status");
            if (!st.equals("DISPONIVEL") && !st.equals("INATIVO")) return "<span class=\"suave\">em uso</span>";
            String novo = st.equals("INATIVO") ? "DISPONIVEL" : "INATIVO";
            return Pagina.formEmLinha(r, "/gerente/frota/status", "<input type=\"hidden\" name=\"id\" value=\""
                    + frota.texto(i, "id") + "\"><input type=\"hidden\" name=\"status\" value=\"" + novo
                    + "\"><button class=\"sec pequeno\" type=\"submit\">" + (novo.equals("INATIVO") ? "Desativar" : "Reativar") + "</button>");
        });
        String form = Pagina.form(r, "/gerente/frota", "<div class=\"duas\">"
                + Pagina.campo(r, "Placa", "placa", "text", "", "required maxlength=\"8\"")
                + Pagina.campo(r, "Marca", "marca", "text", "", "required")
                + Pagina.campo(r, "Modelo", "modelo", "text", "", "required")
                + Pagina.campo(r, "Ano", "ano", "number", "", "required min=\"1990\"")
                + Pagina.campo(r, "Cor", "cor", "text", "", "required")
                + Pagina.select(r, "Categoria", "categoria", cats, true)
                + Pagina.select(r, "Loja", "loja", lojas, true)
                + Pagina.campo(r, "Quilometragem", "km", "number", "0", "required min=\"0\"")
                + Pagina.campo(r, "Tanque (litros)", "tanque", "number", "", "required min=\"1\"")
                + "</div><p><button type=\"submit\">Cadastrar veículo</button></p>");
        return Resposta.html(Pagina.layout(r, "Frota", Pagina.cartao("Veículos", tabela) + Pagina.cartao("Novo veículo", form)));
    }

    private Resposta cadastrarVeiculo(Requisicao r) throws Exception {
        try {
            Veiculo v = new Veiculo();
            v.setPlaca(r.param("placa"));
            v.setMarca(r.param("marca"));
            v.setModelo(r.param("modelo"));
            v.setAno(r.inteiro("ano"));
            v.setCor(r.param("cor"));
            v.setCategoriaId(r.inteiro("categoria"));
            v.setLojaAtualId(r.inteiro("loja"));
            v.setQuilometragem(r.inteiro("km"));
            v.setCapacidadeTanqueLitros(r.inteiro("tanque"));
            new GerenciaService(r.usuario()).cadastrarVeiculo(v);
            r.sessao().avisar("ok", "Veículo " + v.getPlaca() + " cadastrado.");
            return Resposta.redirecionar("/gerente/frota");
        } catch (NegocioException e) {
            r.erro(e.getMessage());
            return telaFrota(r);
        }
    }

    private Resposta statusVeiculo(Requisicao r) throws Exception {
        try {
            new GerenciaService(r.usuario()).alterarStatusVeiculo(r.inteiro("id"), StatusVeiculo.valueOf(r.param("status")));
            r.sessao().avisar("ok", "Situação do veículo atualizada.");
        } catch (IllegalArgumentException | NegocioException e) {
            r.sessao().avisar("erro", e instanceof NegocioException ? e.getMessage() : "Situação inválida.");
        }
        return Resposta.redirecionar("/gerente/frota");
    }

    // --------------------------------------------------------------- categorias

    private Resposta telaCategorias(Requisicao r) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (CategoriaVeiculo c : new GerenciaService(r.usuario()).categorias()) {
            sb.append(Pagina.cartao(c.getNome(), Pagina.form(r, "/gerente/categorias",
                    "<input type=\"hidden\" name=\"id\" value=\"" + c.getId() + "\"><div class=\"duas\">"
                  + campoPreco(c.getId() + "_diaria", "Diária (R$)", c.getValorDiaria(), "diaria")
                  + campoPreco(c.getId() + "_hora", "Hora extra (R$)", c.getValorHoraExtra(), "hora")
                  + campoPreco(c.getId() + "_taxa", "Taxa de devolução em outra loja (R$)", c.getTaxaDevolucaoOutraLoja(), "taxa")
                  + campoPreco(c.getId() + "_cautela", "Cautela (R$)", c.getValorCautela(), "cautela")
                  + "</div><p><button type=\"submit\">Salvar preços</button></p>")));
        }
        return Resposta.html(Pagina.layout(r, "Categorias e preços",
                "<p class=\"suave\">Alterações de preço ficam registradas na auditoria. Locações já abertas mantêm a diária contratada.</p>" + sb));
    }

    private String campoPreco(String id, String rotulo, BigDecimal valor, String nome) {
        return "<label for=\"" + id + "\">" + Pagina.esc(rotulo) + "</label><input id=\"" + id + "\" name=\"" + nome
             + "\" type=\"text\" inputmode=\"decimal\" required value=\"" + valor.toPlainString() + "\">";
    }

    private Resposta salvarCategoria(Requisicao r) throws Exception {
        try {
            CategoriaVeiculo c = new CategoriaVeiculo();
            c.setId(r.inteiro("id"));
            c.setValorDiaria(r.decimal("diaria"));
            c.setValorHoraExtra(r.decimal("hora"));
            c.setTaxaDevolucaoOutraLoja(r.decimal("taxa"));
            c.setValorCautela(r.decimal("cautela"));
            new GerenciaService(r.usuario()).atualizarPrecos(c);
            r.sessao().avisar("ok", "Preços atualizados.");
        } catch (NegocioException | NullPointerException e) {
            r.sessao().avisar("erro", e instanceof NegocioException ? e.getMessage() : "Preencha todos os valores.");
        }
        return Resposta.redirecionar("/gerente/categorias");
    }

    // --------------------------------------------------------------- manutenção

    private Resposta telaManutencao(Requisicao r) throws Exception {
        GerenciaService g = new GerenciaService(r.usuario());
        Tabela abertas = g.manutencoes(true);
        List<String[]> veiculos = new ArrayList<>();
        for (Veiculo v : g.veiculosParaManutencao()) veiculos.add(new String[]{String.valueOf(v.getId()), v.getDescricao()});
        List<String[]> tipos = new ArrayList<>();
        for (TipoManutencao t : TipoManutencao.values()) tipos.add(new String[]{t.name(), t.name().charAt(0) + t.name().substring(1).toLowerCase()});
        String form = Pagina.form(r, "/gerente/manutencao", "<div class=\"duas\">"
                + Pagina.select(r, "Veículo (apenas disponíveis)", "veiculo", veiculos, true)
                + Pagina.select(r, "Tipo", "tipo", tipos, true)
                + Pagina.campo(r, "Descrição", "descricao", "text", "", "required maxlength=\"200\"")
                + "</div><p><button type=\"submit\">Enviar para manutenção</button></p>");
        String corpo = Pagina.cartao("Nova manutenção", form)
                + Pagina.cartao("Em andamento", Pagina.tabela(abertas, "Concluir", i ->
                    Pagina.formEmLinha(r, "/gerente/manutencao/concluir", "<input type=\"hidden\" name=\"id\" value=\"" + abertas.texto(i, "id")
                        + "\"><input name=\"custo\" placeholder=\"Custo (R$)\" inputmode=\"decimal\" required>"
                        + "<button class=\"sec pequeno\" type=\"submit\">Concluir</button>")))
                + Pagina.cartao("Histórico", Pagina.tabela(g.manutencoes(false)));
        return Resposta.html(Pagina.layout(r, "Manutenção", corpo));
    }

    private Resposta abrirManutencao(Requisicao r) throws Exception {
        try {
            Manutencao m = new Manutencao();
            m.setVeiculoId(r.inteiro("veiculo"));
            m.setTipo(TipoManutencao.valueOf(r.param("tipo")));
            m.setDescricao(r.param("descricao"));
            new GerenciaService(r.usuario()).abrirManutencao(m);
            r.sessao().avisar("ok", "Veículo enviado para manutenção; ele não aparece mais como disponível.");
            return Resposta.redirecionar("/gerente/manutencao");
        } catch (IllegalArgumentException e) {
            r.erro("Tipo de manutenção inválido.");
            return telaManutencao(r);
        } catch (NegocioException e) {
            r.erro(e.getMessage());
            return telaManutencao(r);
        }
    }

    private Resposta concluirManutencao(Requisicao r) throws Exception {
        try {
            new GerenciaService(r.usuario()).concluirManutencao(r.inteiro("id"), r.decimal("custo"));
            r.sessao().avisar("ok", "Manutenção concluída; o veículo voltou à frota.");
        } catch (NegocioException e) {
            r.sessao().avisar("erro", e.getMessage());
        }
        return Resposta.redirecionar("/gerente/manutencao");
    }

    // ------------------------------------------------------------- funcionários

    private Resposta telaFuncionarios(Requisicao r) throws Exception {
        GerenciaService g = new GerenciaService(r.usuario());
        Tabela func = g.funcionarios();
        List<String[]> lojas = new ArrayList<>();
        for (Loja l : g.lojas()) lojas.add(new String[]{String.valueOf(l.getId()), l.getRotulo()});
        List<String[]> cargos = new ArrayList<>();
        for (Cargo c : Cargo.values()) cargos.add(new String[]{c.name(), c.name().charAt(0) + c.name().substring(1).toLowerCase()});
        String tabela = Pagina.tabela(func, "Ações", i -> {
            boolean ativo = Boolean.TRUE.equals(func.valor(i, "ativo"));
            return Pagina.formEmLinha(r, "/gerente/funcionarios/ativo", "<input type=\"hidden\" name=\"id\" value=\"" + func.texto(i, "id")
                    + "\"><input type=\"hidden\" name=\"ativo\" value=\"" + !ativo + "\"><button class=\"sec pequeno\" type=\"submit\">"
                    + (ativo ? "Desativar" : "Reativar") + "</button>");
        });
        String form = Pagina.form(r, "/gerente/funcionarios", "<div class=\"duas\">"
                + Pagina.campo(r, "Nome", "nome", "text", "", "required")
                + Pagina.campo(r, "E-mail", "email", "email", "", "required")
                + Pagina.campo(r, "Telefone", "telefone", "tel", "", "")
                + Pagina.campo(r, "CPF (só números)", "cpf", "text", "", "required pattern=\"\\d{11}\" maxlength=\"11\"")
                + Pagina.select(r, "Cargo", "cargo", cargos, true)
                + Pagina.select(r, "Loja", "loja", lojas, true)
                + Pagina.campo(r, "Senha inicial", "senha", "password", "", "required minlength=\"6\"")
                + "</div><p><button type=\"submit\">Cadastrar funcionário</button></p>");
        return Resposta.html(Pagina.layout(r, "Funcionários", Pagina.cartao("Equipe", tabela) + Pagina.cartao("Novo funcionário", form)));
    }

    private Resposta cadastrarFuncionario(Requisicao r) throws Exception {
        try {
            Funcionario f = new Funcionario(null, r.param("nome"), r.param("email"), r.param("telefone"), r.param("cpf"),
                    Cargo.valueOf(r.param("cargo")), r.inteiro("loja"));
            new GerenciaService(r.usuario()).cadastrarFuncionario(f, r.param("senha"));
            r.sessao().avisar("ok", "Funcionário cadastrado.");
            return Resposta.redirecionar("/gerente/funcionarios");
        } catch (IllegalArgumentException e) {
            r.erro("Cargo inválido.");
            return telaFuncionarios(r);
        } catch (NegocioException e) {
            r.erro(e.getMessage());
            return telaFuncionarios(r);
        }
    }

    private Resposta ativoFuncionario(Requisicao r) throws Exception {
        try {
            new GerenciaService(r.usuario()).alterarAtivo(r.inteiro("id"), Boolean.parseBoolean(r.param("ativo")));
            r.sessao().avisar("ok", "Funcionário atualizado.");
        } catch (NegocioException e) {
            r.sessao().avisar("erro", e.getMessage());
        }
        return Resposta.redirecionar("/gerente/funcionarios");
    }
}
