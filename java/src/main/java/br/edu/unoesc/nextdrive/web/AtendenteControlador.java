package br.edu.unoesc.nextdrive.web;

import br.edu.unoesc.nextdrive.model.ClientePessoaFisica;
import br.edu.unoesc.nextdrive.model.ClientePessoaJuridica;
import br.edu.unoesc.nextdrive.model.FormaPagamento;
import br.edu.unoesc.nextdrive.model.Locacao;
import br.edu.unoesc.nextdrive.model.Loja;
import br.edu.unoesc.nextdrive.model.Multa;
import br.edu.unoesc.nextdrive.model.Pagamento;
import br.edu.unoesc.nextdrive.model.PagamentoCartao;
import br.edu.unoesc.nextdrive.model.PagamentoPix;
import br.edu.unoesc.nextdrive.model.Veiculo;
import br.edu.unoesc.nextdrive.service.AutenticacaoService;
import br.edu.unoesc.nextdrive.service.LocacaoService;
import br.edu.unoesc.nextdrive.service.NegocioException;
import br.edu.unoesc.nextdrive.service.ReservaService;
import br.edu.unoesc.nextdrive.util.Tabela;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Telas do balcão: painel, cancelamento, retirada, devolução, pagamento, multas e clientes. */
public class AtendenteControlador {
    private static final String[] BALCAO = {"ATENDENTE", "GERENTE"};

    /** Registra as rotas deste controlador. */
    public void registrar(Servidor s) {
        s.rota("GET", "/atendente", this::painel, BALCAO);
        s.rota("POST", "/atendente/cancelar", this::cancelar, BALCAO);
        s.rota("GET", "/atendente/retirada", r -> telaRetirada(r), BALCAO);
        s.rota("POST", "/atendente/retirada", this::retirar, BALCAO);
        s.rota("GET", "/atendente/devolucao", r -> telaDevolucao(r), BALCAO);
        s.rota("POST", "/atendente/devolucao", this::devolver, BALCAO);
        s.rota("GET", "/atendente/pagamento", r -> telaPagamento(r), BALCAO);
        s.rota("POST", "/atendente/pagamento", this::pagar, BALCAO);
        s.rota("GET", "/atendente/multa", r -> telaMulta(r), BALCAO);
        s.rota("POST", "/atendente/multa", this::registrarMulta, BALCAO);
        s.rota("POST", "/atendente/multa/pagar", this::pagarMulta, BALCAO);
        s.rota("GET", "/atendente/clientes", r -> telaClientes(r), BALCAO);
        s.rota("GET", "/atendente/cliente/novo", r -> telaNovoCliente(r), BALCAO);
        s.rota("POST", "/atendente/cliente/novo", this::cadastrarCliente, BALCAO);
    }

    // ------------------------------------------------------------------ painel

    private Resposta painel(Requisicao r) throws Exception {
        LocacaoService loc = new LocacaoService(r.usuario());
        Tabela reservas = new ReservaService(r.usuario()).ativas();
        String tabReservas = Pagina.tabela(reservas, "Ações", i ->
                "<a class=\"botao pequeno\" href=\"/atendente/retirada?reserva=" + reservas.texto(i, "id") + "\">Retirada</a> "
              + Pagina.formEmLinha(r, "/atendente/cancelar",
                    "<input type=\"hidden\" name=\"reserva\" value=\"" + reservas.texto(i, "id") + "\">"
                  + "<input name=\"justificativa\" placeholder=\"Motivo do cancelamento\" required>"
                  + "<button class=\"sec pequeno\" type=\"submit\">Cancelar</button>"));
        Tabela abertas = loc.abertas();
        Tabela pagar = loc.aguardandoPagamento();
        Tabela multas = loc.multasPendentes();
        String corpo = "<p class=\"linha\"><a class=\"botao\" href=\"/reserva/nova\">Nova reserva</a>"
                + "<a class=\"botao sec\" href=\"/atendente/cliente/novo\">Novo cliente</a>"
                + "<a class=\"botao sec\" href=\"/atendente/multa\">Registrar multa</a></p>"
                + Pagina.cartao("Reservas ativas", tabReservas)
                + Pagina.cartao("Veículos na rua (locações abertas)", Pagina.tabela(abertas, "Ações", i ->
                    "<a class=\"botao pequeno\" href=\"/atendente/devolucao?locacao=" + abertas.texto(i, "id") + "\">Devolução</a>"))
                + Pagina.cartao("Aguardando pagamento", Pagina.tabela(pagar, "Ações", i ->
                    "<a class=\"botao pequeno\" href=\"/atendente/pagamento?locacao=" + pagar.texto(i, "id") + "\">Receber</a>"))
                + Pagina.cartao("Multas pendentes", Pagina.tabela(multas, "Ações", i ->
                    Pagina.formEmLinha(r, "/atendente/multa/pagar",
                        "<input type=\"hidden\" name=\"multa\" value=\"" + multas.texto(i, "id") + "\">"
                      + "<button class=\"sec pequeno\" type=\"submit\">Marcar como paga</button>")));
        return Resposta.html(Pagina.layout(r, "Painel do balcão", corpo));
    }

    private Resposta cancelar(Requisicao r) throws Exception {
        try {
            new ReservaService(r.usuario()).cancelar(r.inteiro("reserva"), r.param("justificativa"));
            r.sessao().avisar("ok", "Reserva #" + r.param("reserva") + " cancelada.");
        } catch (NegocioException e) {
            r.sessao().avisar("erro", e.getMessage());
        }
        return Resposta.redirecionar("/atendente");
    }

    // ---------------------------------------------------------------- retirada

    private Resposta telaRetirada(Requisicao r) throws Exception {
        int reserva = r.inteiro("reserva");
        LocacaoService loc = new LocacaoService(r.usuario());
        ClientePessoaFisica condutor = loc.condutorDaReserva(reserva);
        List<Veiculo> veiculos = loc.veiculosParaRetirada(reserva);
        String problema = condutor.validarCondutor(LocalDate.now());

        String resumo = Pagina.dados(
                "Reserva", "#" + reserva,
                "Condutor", Pagina.esc(condutor.getNome()),
                "CPF", Pagina.esc(condutor.getCpf()),
                "Idade", condutor.getIdade(LocalDate.now()) + " anos",
                "CNH", Pagina.esc(condutor.getCnhNumero()) + ", emitida em " + condutor.getCnhDataEmissao()
                        + ", válida até " + condutor.getCnhValidade());
        String alerta = problema == null ? "" : "<div class=\"aviso erro\">" + Pagina.esc(problema)
                + " A retirada será recusada (RN05).</div>";

        List<String[]> opcoes = new ArrayList<>();
        for (Veiculo v : veiculos)
            opcoes.add(new String[]{String.valueOf(v.getId()), v.getDescricao() + " (" + v.getQuilometragem() + " km)"});
        String form = veiculos.isEmpty()
                ? "<div class=\"aviso erro\">Nenhum veículo disponível agora para esta reserva.</div>"
                : Pagina.form(r, "/atendente/retirada",
                    "<input type=\"hidden\" name=\"reserva\" value=\"" + reserva + "\">"
                  + Pagina.select(r, "Veículo", "veiculo", opcoes, true)
                  + "<div class=\"duas\">"
                  + Pagina.campo(r, "Quilometragem de saída", "km_saida", "number", "", "required min=\"0\"")
                  + Pagina.campo(r, "Combustível na saída (%)", "combustivel", "number", "100", "required min=\"0\" max=\"100\"")
                  + "</div><p class=\"suave\">Confira a CNH e registre a cautela no cartão de crédito do cliente.</p>"
                  + "<p><button type=\"submit\">Registrar retirada</button></p>");
        return Resposta.html(Pagina.layout(r, "Registrar retirada", Pagina.cartao("Condutor", resumo + alerta) + Pagina.cartao("Veículo", form)));
    }

    private Resposta retirar(Requisicao r) throws Exception {
        try {
            new LocacaoService(r.usuario()).abrir(r.inteiro("reserva"), r.inteiro("veiculo"),
                    r.inteiro("km_saida"), r.inteiro("combustivel"));
            r.sessao().avisar("ok", "Retirada registrada. Entregue o veículo ao cliente.");
            return Resposta.redirecionar("/atendente");
        } catch (NegocioException e) {
            r.erro(e.getMessage());
            return telaRetirada(r);
        }
    }

    // --------------------------------------------------------------- devolução

    private Resposta telaDevolucao(Requisicao r) throws Exception {
        int id = r.inteiro("locacao");
        LocacaoService loc = new LocacaoService(r.usuario());
        Tabela d = loc.detalhe(id);
        if (d.isVazia()) throw new NegocioException("Locação não encontrada.");
        List<String[]> lojas = new ArrayList<>();
        for (Loja l : new ReservaService(r.usuario()).lojas()) lojas.add(new String[]{String.valueOf(l.getId()), l.getRotulo()});
        String resumo = Pagina.dados(
                "Locação", "#" + id,
                "Cliente", Pagina.esc(d.texto(0, "cliente")),
                "Veículo", Pagina.esc(d.texto(0, "placa") + " - " + d.texto(0, "veiculo")),
                "Retirada", Pagina.formatar(d.valor(0, "data_retirada"), "") + " em " + Pagina.esc(d.texto(0, "loja_retirada")),
                "Devolução prevista", Pagina.formatar(d.valor(0, "data_devolucao_prevista"), ""),
                "Situação", Pagina.formatar(d.valor(0, "status"), "status"));
        String form = "ABERTA".equals(d.texto(0, "status"))
                ? Pagina.form(r, "/atendente/devolucao",
                    "<input type=\"hidden\" name=\"locacao\" value=\"" + id + "\">"
                  + "<div class=\"duas\">"
                  + Pagina.campo(r, "Quilometragem de chegada", "km_chegada", "number", "", "required min=\"0\"")
                  + Pagina.campo(r, "Combustível na chegada (%)", "combustivel", "number", "", "required min=\"0\" max=\"100\"")
                  + Pagina.select(r, "Loja de devolução", "loja_devolucao", lojas, true)
                  + Pagina.campo(r, "Desconto (R$)", "desconto", "text", "0", "inputmode=\"decimal\"")
                  + "</div><p><button type=\"submit\">Registrar devolução e calcular valor</button></p>")
                : "<p class=\"suave\">Esta locação já foi devolvida.</p>";
        return Resposta.html(Pagina.layout(r, "Registrar devolução", Pagina.cartao("Locação", resumo) + Pagina.cartao("Devolução", form)));
    }

    private Resposta devolver(Requisicao r) throws Exception {
        try {
            Locacao l = new LocacaoService(r.usuario()).devolver(r.inteiro("locacao"), r.inteiro("km_chegada"),
                    r.inteiro("combustivel"), r.inteiro("loja_devolucao"), r.decimal("desconto"));
            String corpo = Pagina.cartao("Valor da locação", Pagina.dados(
                    "Diárias", Pagina.moeda(l.getValorDiarias()) + " <span class=\"suave\">(" + Pagina.moeda(l.getValorDiariaContratada()) + " por diária contratada)</span>",
                    "Horas extras", Pagina.moeda(l.getValorHorasExtras()),
                    "Combustível", Pagina.moeda(l.getValorCombustivel()),
                    "Devolução em outra loja", Pagina.moeda(l.getTaxaDevolucaoOutraLoja()),
                    "Desconto", Pagina.moeda(l.getDesconto()),
                    "Quilômetros rodados", l.kmRodados(),
                    "Valor final", "<strong>" + Pagina.moeda(l.getValorFinal()) + "</strong>"))
                    + "<p class=\"linha\"><a class=\"botao\" href=\"/atendente/pagamento?locacao=" + l.getId() + "\">Receber pagamento</a>"
                    + "<a class=\"botao sec\" href=\"/atendente\">Voltar ao painel</a></p>";
            return Resposta.html(Pagina.layout(r, "Devolução registrada", corpo));
        } catch (NegocioException e) {
            r.erro(e.getMessage());
            return telaDevolucao(r);
        }
    }

    // --------------------------------------------------------------- pagamento

    private Resposta telaPagamento(Requisicao r) throws Exception {
        int id = r.inteiro("locacao");
        LocacaoService loc = new LocacaoService(r.usuario());
        Locacao l = loc.buscar(id);
        String resumo = Pagina.dados(
                "Locação", "#" + id,
                "Situação", Pagina.formatar(l.getStatus().name(), "status"),
                "Valor final", l.getValorFinal() == null ? "–" : Pagina.moeda(l.getValorFinal()),
                "Já pago", Pagina.moeda(l.totalPago()),
                "Saldo devedor", "<strong>" + Pagina.moeda(l.saldoDevedor()) + "</strong>",
                "Cautela", l.isCautelaLiberada() ? "Liberada" : "Retida (" + Pagina.moeda(l.getCautelaValor()) + ")");
        List<String[]> formas = new ArrayList<>();
        for (FormaPagamento f : FormaPagamento.values()) formas.add(new String[]{f.name(), f.getRotulo()});
        String form = l.saldoDevedor().signum() <= 0
                ? "<p class=\"suave\">Nada a receber nesta locação.</p>"
                : Pagina.form(r, "/atendente/pagamento",
                    "<input type=\"hidden\" name=\"locacao\" value=\"" + id + "\">"
                  + "<div class=\"duas\">"
                  + Pagina.select(r, "Forma de pagamento", "forma", formas, true)
                  + Pagina.campo(r, "Valor (R$)", "valor", "text", l.saldoDevedor().toPlainString(), "required inputmode=\"decimal\"")
                  + "</div><h2>Cartão</h2><div class=\"duas\">"
                  + Pagina.campo(r, "Bandeira", "bandeira", "text", "", "")
                  + Pagina.campo(r, "Final do cartão (4 dígitos)", "final_cartao", "text", "", "maxlength=\"4\" inputmode=\"numeric\"")
                  + Pagina.campo(r, "Parcelas (só crédito)", "parcelas", "number", "1", "min=\"1\" max=\"12\"")
                  + "</div><h2>PIX</h2><div class=\"duas\">"
                  + Pagina.campo(r, "Código da transação (txid)", "txid", "text", "", "maxlength=\"35\"")
                  + Pagina.campo(r, "Chave PIX (opcional)", "chave_pix", "text", "", "")
                  + "</div><p><button type=\"submit\">Registrar pagamento</button></p>");
        return Resposta.html(Pagina.layout(r, "Receber pagamento", Pagina.cartao("Locação", resumo)
                + Pagina.cartao("Novo pagamento", form)
                + Pagina.cartao("Pagamentos já registrados", Pagina.tabela(loc.pagamentos(id)))));
    }

    private Resposta pagar(Requisicao r) throws Exception {
        try {
            int locacao = r.inteiro("locacao");
            BigDecimal valor = r.decimal("valor");
            if (valor == null) throw new NegocioException("Informe o valor.");
            FormaPagamento forma = FormaPagamento.valueOf(r.param("forma"));
            Pagamento p = forma.usaCartao()
                    ? new PagamentoCartao(locacao, valor, forma, r.param("bandeira"), r.param("final_cartao"),
                            forma == FormaPagamento.CARTAO_DEBITO ? 1 : Integer.parseInt(r.param("parcelas", "1")))
                    : new PagamentoPix(locacao, valor, r.param("txid"), r.param("chave_pix"));
            Locacao l = new LocacaoService(r.usuario()).pagar(p);
            r.sessao().avisar("ok", l.saldoDevedor().signum() == 0
                    ? "Pagamento registrado. Locação quitada!" : "Pagamento registrado. Saldo restante: " + Pagina.moeda(l.saldoDevedor()));
            return Resposta.redirecionar("/atendente/pagamento?locacao=" + locacao);
        } catch (IllegalArgumentException e) {
            r.erro("Dados do pagamento inválidos.");
            return telaPagamento(r);
        } catch (NegocioException e) {
            r.erro(e.getMessage());
            return telaPagamento(r);
        }
    }

    // ------------------------------------------------------------------ multas

    private Resposta telaMulta(Requisicao r) throws Exception {
        String form = Pagina.form(r, "/atendente/multa",
                "<div class=\"duas\">"
              + Pagina.campo(r, "Número da locação", "locacao", "number", "", "required min=\"1\"")
              + Pagina.campo(r, "Valor da multa (R$)", "valor", "text", "", "required inputmode=\"decimal\"")
              + Pagina.campo(r, "Data e hora da infração", "infracao", "datetime-local", "", "required")
              + Pagina.campo(r, "Descrição", "descricao", "text", "", "required maxlength=\"200\"")
              + "</div><p><button type=\"submit\">Registrar multa</button></p>");
        return Resposta.html(Pagina.layout(r, "Multas de trânsito", Pagina.cartao("Nova multa", form)
                + Pagina.cartao("Multas pendentes", Pagina.tabela(new LocacaoService(r.usuario()).multasPendentes()))));
    }

    private Resposta registrarMulta(Requisicao r) throws Exception {
        try {
            Multa m = new Multa();
            m.setLocacaoId(r.inteiro("locacao"));
            m.setDescricao(r.param("descricao"));
            m.setValor(r.decimal("valor"));
            m.setDataInfracao(r.dataHora("infracao"));
            new LocacaoService(r.usuario()).registrarMulta(m);
            r.sessao().avisar("ok", "Multa registrada e vinculada à locação #" + m.getLocacaoId() + ".");
            return Resposta.redirecionar("/atendente/multa");
        } catch (NegocioException e) {
            r.erro(e.getMessage());
            return telaMulta(r);
        }
    }

    private Resposta pagarMulta(Requisicao r) throws Exception {
        try {
            new LocacaoService(r.usuario()).pagarMulta(r.inteiro("multa"));
            r.sessao().avisar("ok", "Multa marcada como paga.");
        } catch (NegocioException e) {
            r.sessao().avisar("erro", e.getMessage());
        }
        return Resposta.redirecionar("/atendente");
    }

    // ----------------------------------------------------------------- clientes

    private Resposta telaClientes(Requisicao r) throws Exception {
        AutenticacaoService servico = new AutenticacaoService(r.usuario());
        Tabela clientes = servico.clientes();
        String corpo = Pagina.cartao("Clientes", Pagina.tabela(clientes, "Histórico", i ->
                "<a class=\"botao sec pequeno\" href=\"/atendente/clientes?id=" + clientes.texto(i, "id") + "\">Ver locações</a>"));
        if (r.tem("id")) {
            corpo += Pagina.cartao("Histórico de locações do cliente #" + Pagina.esc(r.param("id")),
                    Pagina.tabela(servico.historico(r.inteiro("id"))));
        }
        return Resposta.html(Pagina.layout(r, "Clientes", corpo));
    }

    private Resposta telaNovoCliente(Requisicao r) throws Exception {
        boolean pj = "PJ".equals(r.param("tipo"));
        String escolha = "<p class=\"linha\"><a class=\"botao" + (pj ? " sec" : "") + "\" href=\"/atendente/cliente/novo?tipo=PF\">Pessoa física</a>"
                + "<a class=\"botao" + (pj ? "" : " sec") + "\" href=\"/atendente/cliente/novo?tipo=PJ\">Pessoa jurídica</a></p>";
        String form;
        if (!pj) {
            form = Pagina.form(r, "/atendente/cliente/novo", "<input type=\"hidden\" name=\"tipo\" value=\"PF\">"
                    + AutenticacaoControlador.camposPF(r, "") + "<p><button type=\"submit\">Cadastrar cliente</button></p>");
        } else {
            Tabela condutores = new AutenticacaoService(r.usuario()).condutores();
            List<String[]> opcoes = new ArrayList<>();
            for (int i = 0; i < condutores.getLinhas().size(); i++)
                opcoes.add(new String[]{condutores.texto(i, "id"), condutores.texto(i, "nome") + " (CPF " + condutores.texto(i, "documento") + ")"});
            form = Pagina.form(r, "/atendente/cliente/novo", "<input type=\"hidden\" name=\"tipo\" value=\"PJ\"><div class=\"duas\">"
                    + Pagina.campo(r, "Nome fantasia", "nome", "text", "", "required")
                    + Pagina.campo(r, "Razão social", "razao_social", "text", "", "required")
                    + Pagina.campo(r, "CNPJ (14 números)", "cnpj", "text", "", "required pattern=\"\\d{14}\" maxlength=\"14\" inputmode=\"numeric\"")
                    + Pagina.campo(r, "E-mail", "email", "email", "", "required")
                    + Pagina.campo(r, "Telefone", "telefone", "tel", "", "")
                    + Pagina.campo(r, "Senha inicial", "senha", "password", "", "required minlength=\"6\"")
                    + Pagina.select(r, "Condutor responsável (pessoa física já cadastrada)", "condutor", opcoes, true)
                    + "</div><p><button type=\"submit\">Cadastrar empresa</button></p>");
        }
        return Resposta.html(Pagina.layout(r, "Novo cliente", Pagina.cartao(null, escolha + form)));
    }

    private Resposta cadastrarCliente(Requisicao r) throws Exception {
        AutenticacaoService servico = new AutenticacaoService(r.usuario());
        try {
            if ("PJ".equals(r.param("tipo"))) {
                ClientePessoaFisica condutor = new ClientePessoaFisica();
                condutor.setId(r.inteiro("condutor"));
                servico.cadastrarPJ(new ClientePessoaJuridica(null, r.param("nome"), r.param("email"), r.param("telefone"),
                        r.param("cnpj"), r.param("razao_social"), condutor), r.param("senha"));
            } else {
                servico.cadastrarPF(AutenticacaoControlador.lerPF(r, ""), r.param("senha"));
            }
            r.sessao().avisar("ok", "Cliente cadastrado.");
            return Resposta.redirecionar("/atendente/clientes");
        } catch (NegocioException e) {
            r.erro(e.getMessage());
            return telaNovoCliente(r);
        }
    }
}
