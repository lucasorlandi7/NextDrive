package br.edu.unoesc.nextdrive.model;

/** Loja da locadora, usada como ponto de retirada e devolução. */
public class Loja {
    private Integer id;
    private String nome;
    private String cidade;
    private String uf;
    private String endereco;
    private String telefone;
    private boolean ativa = true;

    public Loja() { }

    public Loja(Integer id, String nome, String cidade, String uf) {
        this.id = id;
        this.nome = nome;
        this.cidade = cidade;
        this.uf = uf;
    }

    /** @return o nome com a cidade, para listas de seleção */
    public String getRotulo() { return nome + " - " + cidade + "/" + uf; }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }
    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf; }
    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public boolean isAtiva() { return ativa; }
    public void setAtiva(boolean ativa) { this.ativa = ativa; }
}
