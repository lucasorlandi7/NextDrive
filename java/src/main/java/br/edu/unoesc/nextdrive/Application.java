package br.edu.unoesc.nextdrive;

import br.edu.unoesc.nextdrive.dao.SingleConnection;

import java.awt.Desktop;
import java.net.URI;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** Inicia o Next Drive: confere o banco, liga o servidor e abre o site no navegador. */
@SpringBootApplication
public class Application {

    /**
     * Inicia o sistema.
     *
     * @param args não utilizado
     */
    public static void main(String[] args) {
        if (SingleConnection.getConnection() == null) {
            System.out.println("Não foi possível conectar ao banco. Confira o PostgreSQL e a senha em SingleConnection.");
            return;
        }
        new SpringApplicationBuilder(Application.class).headless(false).run(args);
        String endereco = "http://localhost:8080";
        System.out.println("Next Drive no ar: " + endereco);
        try {
            Desktop.getDesktop().browse(new URI(endereco));
        } catch (Exception e) {
            System.out.println("Abra o endereço acima no navegador.");
        }
    }
}
