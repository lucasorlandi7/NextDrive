package br.edu.unoesc.nextdrive.web;

import br.edu.unoesc.nextdrive.model.CategoriaVeiculo;
import br.edu.unoesc.nextdrive.model.Loja;
import br.edu.unoesc.nextdrive.model.Reserva;
import br.edu.unoesc.nextdrive.service.AutenticacaoService;
import br.edu.unoesc.nextdrive.service.NegocioException;
import br.edu.unoesc.nextdrive.service.ReservaService;
import br.edu.unoesc.nextdrive.util.Tabela;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Tela de reserva (RF06, RF07), usada pelo cliente no site e pelo atendente no balcão. */
public class ReservaControlador {

    /** Registra as rotas deste controlador. */
    public void registrar(Servidor s) {
        s.rota("GET", "/reserva/nova", r -> tela(r, null), "CLIENTE", "ATENDENTE", "GERENTE");
        s.rota("POST", "/reserva/nova", this::processar, "CLIENTE", "ATENDENTE", "GERENTE");
    }

    private Resposta tela(Requisicao r, ReservaService.Disponibilidade resultado) throws NegocioException {
        ReservaService servico = new ReservaService(r.usuario());
        List<String[]> lojas = new ArrayList<>();
        for (Loja l : servico.lojas()) lojas.add(new String[]{String.valueOf(l.getId()), l.getRotulo()});
        List<String[]> categorias = new ArrayList<>();
        for (CategoriaVeiculo c : servico.categorias())
            categorias.add(new String[]{String.valueOf(c.getId()), c.getNome() + " - " + Pagina.moeda(c.getValorDiaria()) + "/dia"});

        StringBuilder campos = new StringBuilder();
        if (r.usuario().isFuncionario()) {
            Tabela clientes = new AutenticacaoService(r.usuario()).clientes();
            List<String[]> opcoes = new ArrayList<>();
            for (int i = 0; i < clientes.getLinhas().size(); i++)
                opcoes.add(new String[]{clientes.texto(i, "id"),
                        clientes.texto(i, "nome") + " (" + clientes.texto(i, "tipo") + " " + clientes.texto(i, "documento") + ")"});
            campos.append(Pagina.select(r, "Cliente", "cliente_id", opcoes, true));
        }
        LocalDateTime amanha = LocalDateTime.now().plusDays(1).withMinute(0).withSecond(0).withNano(0);
        campos.append(Pagina.select(r, "Categoria", "categoria_id", categorias, true))
              .append("<div class=\"duas\">")
              .append(Pagina.select(r, "Loja de retirada", "loja_retirada_id", lojas, true))
              .append(Pagina.select(r, "Loja de devolução", "loja_devolucao_id", lojas, true))
              .append(Pagina.campo(r, "Retirada", "retirada", "datetime-local", Pagina.paraCampo(amanha), "required"))
              .append(Pagina.campo(r, "Devolução", "devolucao", "datetime-local", Pagina.paraCampo(amanha.plusDays(3)), "required"))
              .append("</div>");

        String corpo;
        if (resultado == null) {
            corpo = Pagina.cartao("Dados da reserva", Pagina.form(r, "/reserva/nova", campos
                    + "<input type=\"hidden\" name=\"acao\" value=\"consultar\">"
                    + "<p><button type=\"submit\">Consultar disponibilidade</button></p>"));
        } else {
            String situacao = resultado.isDisponivel()
                    ? "<div class=\"aviso ok\">Há veículo disponível. Valor estimado: <strong>" + Pagina.moeda(resultado.getEstimativa())
                      + "</strong> (cada 24 horas, ou fração, é uma diária; o valor final é calculado na devolução).</div>"
                    : "<div class=\"aviso erro\">Sem veículos disponíveis nesse período (" + resultado.getLivres()
                      + " livres, " + resultado.getReservadas() + " já reservados). Tente outras datas, loja ou categoria.</div>";
            String confirmar = resultado.isDisponivel()
                    ? "<input type=\"hidden\" name=\"acao\" value=\"confirmar\"><p><button type=\"submit\">Confirmar reserva</button> "
                      + "<a class=\"botao sec\" href=\"/reserva/nova\">Recomeçar</a></p>"
                    : "<input type=\"hidden\" name=\"acao\" value=\"consultar\"><p><button type=\"submit\">Consultar de novo</button></p>";
            corpo = Pagina.cartao("Resultado da consulta", situacao + Pagina.form(r, "/reserva/nova", campos + confirmar));
        }
        return Resposta.html(Pagina.layout(r, "Nova reserva", corpo));
    }

    private Resposta processar(Requisicao r) throws Exception {
        try {
            ReservaService servico = new ReservaService(r.usuario());
            Reserva reserva = new Reserva();
            reserva.setClienteId(r.usuario().isFuncionario() ? r.inteiro("cliente_id") : r.usuario().getId());
            reserva.setCategoriaId(r.inteiro("categoria_id"));
            reserva.setLojaRetiradaId(r.inteiro("loja_retirada_id"));
            reserva.setLojaDevolucaoId(r.inteiro("loja_devolucao_id"));
            reserva.setDataRetiradaPrevista(r.dataHora("retirada"));
            reserva.setDataDevolucaoPrevista(r.dataHora("devolucao"));

            if ("confirmar".equals(r.param("acao"))) {
                int id = servico.criar(reserva, r.usuario());
                r.sessao().avisar("ok", "Reserva #" + id + " confirmada!");
                return Resposta.redirecionar(r.usuario().isFuncionario() ? "/atendente" : "/cliente");
            }
            return tela(r, servico.consultar(reserva.getCategoriaId(), reserva.getLojaRetiradaId(),
                    reserva.getDataRetiradaPrevista(), reserva.getDataDevolucaoPrevista()));
        } catch (NegocioException e) {
            r.erro(e.getMessage());
            return tela(r, null);
        }
    }
}
