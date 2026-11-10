package br.edu.unoesc.nextdrive;

import br.edu.unoesc.nextdrive.util.Conexao;
import br.edu.unoesc.nextdrive.web.AtendenteControlador;
import br.edu.unoesc.nextdrive.web.AutenticacaoControlador;
import br.edu.unoesc.nextdrive.web.ClienteControlador;
import br.edu.unoesc.nextdrive.web.GerenteControlador;
import br.edu.unoesc.nextdrive.web.ReservaControlador;
import br.edu.unoesc.nextdrive.web.Servidor;

/**
 * Ponto de entrada do sistema Next Drive. Sobe o servidor web na porta 8080
 * (ou na definida em {@code NEXTDRIVE_PORT}) e registra as telas de cada perfil.
 */
public final class App {

    private App() { }

    /**
     * @param args não utilizado
     * @throws Exception se o servidor não puder ser iniciado
     */
    public static void main(String[] args) throws Exception {
        String porta = System.getenv("NEXTDRIVE_PORT");
        Servidor servidor = new Servidor(porta == null || porta.isBlank() ? 8080 : Integer.parseInt(porta));
        new AutenticacaoControlador().registrar(servidor);
        new ReservaControlador().registrar(servidor);
        new ClienteControlador().registrar(servidor);
        new AtendenteControlador().registrar(servidor);
        new GerenteControlador().registrar(servidor);
        servidor.iniciar();
        System.out.println("Next Drive no ar: http://localhost:" + (porta == null || porta.isBlank() ? "8080" : porta));
        System.out.println("Banco de dados: " + Conexao.getUrl());
    }
}
