package br.edu.unoesc.nextdrive.model;

/** Veículo da frota. */
public class Veiculo {
    private Integer id;
    private String placa;
    private String marca;
    private String modelo;
    private int ano;
    private String cor;
    private Integer categoriaId;
    private Integer lojaAtualId;
    private int quilometragem;
    private int capacidadeTanqueLitros;
    private StatusVeiculo status = StatusVeiculo.DISPONIVEL;

    public Veiculo() { }

    /** @return true se o veículo está livre para ser alugado */
    public boolean estaDisponivel() { return status == StatusVeiculo.DISPONIVEL; }

    /** @return descrição curta para listas, como "ABC1D23 - Fiat Argo" */
    public String getDescricao() { return placa + " - " + marca + " " + modelo; }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public int getAno() { return ano; }
    public void setAno(int ano) { this.ano = ano; }
    public String getCor() { return cor; }
    public void setCor(String cor) { this.cor = cor; }
    public Integer getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Integer c) { this.categoriaId = c; }
    public Integer getLojaAtualId() { return lojaAtualId; }
    public void setLojaAtualId(Integer l) { this.lojaAtualId = l; }
    public int getQuilometragem() { return quilometragem; }
    public void setQuilometragem(int q) { this.quilometragem = q; }
    public int getCapacidadeTanqueLitros() { return capacidadeTanqueLitros; }
    public void setCapacidadeTanqueLitros(int c) { this.capacidadeTanqueLitros = c; }
    public StatusVeiculo getStatus() { return status; }
    public void setStatus(StatusVeiculo status) { this.status = status; }
}
