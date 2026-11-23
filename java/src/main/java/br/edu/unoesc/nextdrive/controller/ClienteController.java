package br.edu.unoesc.nextdrive.controller;

import br.edu.unoesc.nextdrive.dao.ClienteDAO;
import br.edu.unoesc.nextdrive.model.Cliente;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoints de clientes: listar, cadastrar e excluir. */
@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private ClienteDAO dao = new ClienteDAO();

    /**
     * Lista todos os clientes.
     *
     * @return lista de clientes em JSON
     */
    @GetMapping
    public ResponseEntity<Object> listar() {
        return new ResponseEntity<Object>(dao.listar(), HttpStatus.OK);
    }

    /**
     * Cadastra um cliente novo.
     *
     * @param dados nome, cpf, email, cnhNumero e cnhValidade (aaaa-mm-dd)
     * @return mensagem de sucesso ou de erro
     */
    @PostMapping
    public ResponseEntity<Object> salvar(@RequestBody Map<String, String> dados) {
        String nome = texto(dados, "nome");
        String cpf = texto(dados, "cpf");
        String email = texto(dados, "email");
        String cnh = texto(dados, "cnhNumero");
        if (nome.isEmpty() || email.isEmpty()) {
            return erro("Preencha o nome e o e-mail.");
        }
        if (!cpf.matches("\\d{11}")) {
            return erro("O CPF precisa ter 11 números.");
        }
        if (!cnh.matches("\\d{11}")) {
            return erro("A CNH precisa ter 11 números.");
        }
        try {
            LocalDate validade = LocalDate.parse(texto(dados, "cnhValidade"));
            if (!dao.salvar(new Cliente(nome, cpf, email, null, cnh, validade))) {
                return erro("Não foi possível salvar. Confira se o CPF ou o e-mail já estão cadastrados.");
            }
            return new ResponseEntity<Object>("Cliente cadastrado.", HttpStatus.CREATED);
        } catch (DateTimeParseException e) {
            return erro("Informe a validade da CNH.");
        }
    }

    /**
     * Exclui um cliente.
     *
     * @param id código do cliente
     * @return mensagem de sucesso ou de erro
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> excluir(@PathVariable("id") int id) {
        if (!dao.excluir(id)) {
            return erro("Cliente não excluído (não existe ou já alugou um veículo).");
        }
        return new ResponseEntity<Object>("Cliente excluído.", HttpStatus.OK);
    }

    private String texto(Map<String, String> dados, String campo) {
        String valor = dados.get(campo);
        if (valor == null) {
            return "";
        }
        return valor.trim();
    }

    private ResponseEntity<Object> erro(String mensagem) {
        return new ResponseEntity<Object>(mensagem, HttpStatus.BAD_REQUEST);
    }
}
