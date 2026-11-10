package br.edu.unoesc.nextdrive.model;

import java.time.LocalDate;

/** Funcionário da locadora: atendente de balcão ou gerente. */
public class Funcionario extends Pessoa {
    private String cpf;
    private Cargo cargo;
    private Integer lojaId;
    private LocalDate dataAdmissao;
    private boolean ativo = true;

    public Funcionario() { }

    public Funcionario(Integer id, String nome, String email, String telefone, String cpf,
                       Cargo cargo, Integer lojaId) {
        super(id, nome, email, telefone);
        this.cpf = cpf;
        this.cargo = cargo;
        this.lojaId = lojaId;
    }

    /** @return true se o cargo é de gerente */
    public boolean isGerente() { return cargo == Cargo.GERENTE; }

    @Override
    public String getPerfil() { return cargo.name(); }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public Cargo getCargo() { return cargo; }
    public void setCargo(Cargo cargo) { this.cargo = cargo; }
    public Integer getLojaId() { return lojaId; }
    public void setLojaId(Integer lojaId) { this.lojaId = lojaId; }
    public LocalDate getDataAdmissao() { return dataAdmissao; }
    public void setDataAdmissao(LocalDate d) { this.dataAdmissao = d; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
