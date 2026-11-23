package br.edu.unoesc.nextdrive.model;

import java.time.LocalDate;

/** Cliente da locadora: a pessoa que aluga o veículo. A CNH é de quem vai dirigir. */
public class Cliente {

    private int id;
    private String nome;
    private String cpf;
    private String email;
    private String telefone;
    private String cnhNumero;
    private LocalDate cnhValidade;

    /**
     * Cria um cliente novo (o código é gerado pelo banco ao salvar).
     *
     * @param nome        nome completo
     * @param cpf         CPF, só números (11 dígitos)
     * @param email       e-mail, único no sistema
     * @param telefone    telefone para contato
     * @param cnhNumero   número da CNH (11 dígitos)
     * @param cnhValidade data de validade da CNH
     */
    public Cliente(String nome, String cpf, String email, String telefone,
                   String cnhNumero, LocalDate cnhValidade) {
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.telefone = telefone;
        this.cnhNumero = cnhNumero;
        this.cnhValidade = cnhValidade;
    }

    /**
     * Confere se a CNH ainda vale em uma data.
     *
     * @param data data a conferir (por exemplo, a devolução do veículo)
     * @return true se a CNH não está vencida nessa data
     */
    public boolean cnhValidaEm(LocalDate data) {
        return !cnhValidade.isBefore(data);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getCnhNumero() {
        return cnhNumero;
    }

    public void setCnhNumero(String cnhNumero) {
        this.cnhNumero = cnhNumero;
    }

    public LocalDate getCnhValidade() {
        return cnhValidade;
    }

    public void setCnhValidade(LocalDate cnhValidade) {
        this.cnhValidade = cnhValidade;
    }

    @Override
    public String toString() {
        return "[" + id + "] " + nome + " - CPF " + cpf + " - " + email
                + " - CNH válida até " + cnhValidade;
    }
}
