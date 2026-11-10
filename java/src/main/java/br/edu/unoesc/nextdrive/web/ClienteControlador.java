package br.edu.unoesc.nextdrive.web;

import br.edu.unoesc.nextdrive.service.AutenticacaoService;
import br.edu.unoesc.nextdrive.service.ReservaService;

/** Área do cliente: suas reservas e o histórico de locações (RF13). */
public class ClienteControlador {

    /** Registra as rotas deste controlador. */
    public void registrar(Servidor s) {
        s.rota("GET", "/cliente", r -> {
            var u = r.usuario();
            String corpo = "<p><a class=\"botao\" href=\"/reserva/nova\">Nova reserva</a></p>"
                    + Pagina.cartao("Minhas reservas", Pagina.tabela(new ReservaService(u).doCliente(u.getId()))
                    + "<p class=\"suave\">Para cancelar uma reserva, procure uma de nossas lojas.</p>")
                    + Pagina.cartao("Minhas locações", Pagina.tabela(new AutenticacaoService(u).historico(u.getId())));
            return Resposta.html(Pagina.layout(r, "Olá, " + u.getNome(), corpo));
        }, "CLIENTE");
    }
}
