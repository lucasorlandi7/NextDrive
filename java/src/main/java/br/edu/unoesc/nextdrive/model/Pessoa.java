package br.edu.unoesc.nextdrive.model;

/**
 * Superclasse abstrata de todas as pessoas com acesso ao sistema (tabela {@code pessoa}).
 * Clientes e funcionários herdam daqui; cada subclasse informa o seu perfil.
 */
public abstract class Pessoa {
    private Integer id;
    private String nome;
    private String email;
    private String telefone;

    protected Pessoa() { }

    protected Pessoa(Integer id, String nome, String email, String telefone) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
    }

    /** @return o perfil de acesso da pessoa (CLIENTE, ATENDENTE ou GERENTE) */
    public abstract String getPerfil();

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    @Override
    public String toString() { return getNome() + " (" + getPerfil() + ")"; }
}
