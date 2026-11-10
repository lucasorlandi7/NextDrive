package br.edu.unoesc.nextdrive.web;

import br.edu.unoesc.nextdrive.model.ClientePessoaFisica;
import br.edu.unoesc.nextdrive.model.Usuario;
import br.edu.unoesc.nextdrive.service.AutenticacaoService;
import br.edu.unoesc.nextdrive.service.NegocioException;
import br.edu.unoesc.nextdrive.service.ReservaService;
import java.util.Optional;

/** Telas públicas: início, login, logout, cadastro de cliente e consulta de disponibilidade. */
public class AutenticacaoControlador {

    /**
     * @param u usuário logado
     * @return a página inicial do perfil
     */
    static String inicio(Usuario u) {
        if (u.isGerente()) return "/gerente";
        return u.isFuncionario() ? "/atendente" : "/cliente";
    }

    /** Registra as rotas deste controlador. */
    public void registrar(Servidor s) {
        s.rota("GET", "/", r -> Resposta.redirecionar(r.usuario() == null ? "/login" : inicio(r.usuario())));
        s.rota("GET", "/login", r -> r.usuario() != null ? Resposta.redirecionar(inicio(r.usuario())) : telaLogin(r));
        s.rota("POST", "/login", this::entrar);
        s.rota("GET", "/sair", r -> {
            if (r.sessao() != null) r.sessao().encerrar();
            return Resposta.redirecionar("/login").comCookie("NDSESSAO=; Path=/; Max-Age=0");
        });
        s.rota("GET", "/cadastro", r -> telaCadastro(r));
        s.rota("POST", "/cadastro", this::cadastrar);
        s.rota("GET", "/frota", this::frota);
    }

    private Resposta telaLogin(Requisicao r) {
        String form = Pagina.form(r, "/login",
                Pagina.campo(r, "E-mail", "email", "email", "", "required autofocus autocomplete=\"username\"")
              + Pagina.campo(r, "Senha", "senha", "password", "", "required autocomplete=\"current-password\"")
              + "<p><button type=\"submit\">Entrar</button></p>");
        String corpo = "<div class=\"grade\"><div>" + Pagina.cartao("Acesse sua conta", form) + "</div><div>"
                + Pagina.cartao("Ainda não tem conta?", "<p>Cadastre-se para reservar veículos pelo site.</p>"
                + "<p><a class=\"botao\" href=\"/cadastro\">Cadastre-se</a> <a class=\"botao sec\" href=\"/frota\">Ver disponibilidade</a></p>")
                + "</div></div>";
        return Resposta.html(Pagina.layout(r, "Entrar", corpo));
    }

    private Resposta entrar(Requisicao r) throws Exception {
        Optional<Usuario> usuario;
        try {
            usuario = new AutenticacaoService().login(r.param("email"), r.param("senha"));
        } catch (NegocioException e) {
            r.erro(e.getMessage());
            return telaLogin(r);
        }
        if (usuario.isEmpty()) {
            r.erro("E-mail ou senha incorretos.");
            return telaLogin(r);
        }
        Sessao nova = Sessao.criar();
        nova.setUsuario(usuario.get());
        return Resposta.redirecionar(inicio(usuario.get())).comCookie(Servidor.cookieDeSessao(nova));
    }

    /** Campos de pessoa física, usados no autocadastro e no cadastro de balcão. */
    static String camposPF(Requisicao r, String prefixo) {
        return "<div class=\"duas\">"
             + Pagina.campo(r, "Nome completo", prefixo + "nome", "text", "", "required")
             + Pagina.campo(r, "E-mail", prefixo + "email", "email", "", "required")
             + Pagina.campo(r, "Telefone", prefixo + "telefone", "tel", "", "")
             + Pagina.campo(r, "Senha (mínimo 6 caracteres)", prefixo + "senha", "password", "", "required minlength=\"6\" autocomplete=\"new-password\"")
             + Pagina.campo(r, "CPF (só números)", prefixo + "cpf", "text", "", "required pattern=\"\\d{11}\" maxlength=\"11\" inputmode=\"numeric\"")
             + Pagina.campo(r, "Data de nascimento", prefixo + "data_nascimento", "date", "", "required")
             + Pagina.campo(r, "Número da CNH (11 números)", prefixo + "cnh_numero", "text", "", "required pattern=\"\\d{11}\" maxlength=\"11\" inputmode=\"numeric\"")
             + Pagina.campo(r, "Emissão da CNH", prefixo + "cnh_data_emissao", "date", "", "required")
             + Pagina.campo(r, "Validade da CNH", prefixo + "cnh_validade", "date", "", "required")
             + "</div>";
    }

    /** Lê os campos de pessoa física enviados pelo formulário. */
    static ClientePessoaFisica lerPF(Requisicao r, String prefixo) throws NegocioException {
        ClientePessoaFisica pf = new ClientePessoaFisica();
        pf.setNome(r.param(prefixo + "nome"));
        pf.setEmail(r.param(prefixo + "email"));
        pf.setTelefone(r.param(prefixo + "telefone"));
        pf.setCpf(r.param(prefixo + "cpf"));
        pf.setDataNascimento(r.data(prefixo + "data_nascimento"));
        pf.setCnhNumero(r.param(prefixo + "cnh_numero"));
        pf.setCnhDataEmissao(r.data(prefixo + "cnh_data_emissao"));
        pf.setCnhValidade(r.data(prefixo + "cnh_validade"));
        return pf;
    }

    private Resposta telaCadastro(Requisicao r) {
        String form = Pagina.form(r, "/cadastro", camposPF(r, "")
                + "<p><button type=\"submit\">Criar conta</button></p>");
        return Resposta.html(Pagina.layout(r, "Cadastre-se",
                Pagina.cartao("Cliente pessoa física", form
                + "<p class=\"suave\">Empresas devem se cadastrar em uma de nossas lojas.</p>")));
    }

    private Resposta cadastrar(Requisicao r) throws Exception {
        try {
            new AutenticacaoService().cadastrarPF(lerPF(r, ""), r.param("senha"));
        } catch (NegocioException e) {
            r.erro(e.getMessage());
            return telaCadastro(r);
        }
        Sessao s = r.sessao() != null ? r.sessao() : Sessao.criar();
        s.avisar("ok", "Cadastro realizado! Entre com seu e-mail e senha.");
        return Resposta.redirecionar("/login").comCookie(Servidor.cookieDeSessao(s));
    }

    private Resposta frota(Requisicao r) throws Exception {
        var tabela = (r.usuario() == null ? new ReservaService() : new ReservaService(r.usuario())).situacaoDaFrota();
        String corpo = Pagina.cartao("Veículos por loja e categoria", Pagina.tabela(tabela)
                + "<p class=\"suave\">Veículos em manutenção não contam como disponíveis.</p>")
                + (r.usuario() == null ? "" : "<p><a class=\"botao\" href=\"/reserva/nova\">Fazer uma reserva</a></p>");
        return Resposta.html(Pagina.layout(r, "Disponibilidade da frota", corpo));
    }
}
