package br.edu.unoesc.nextdrive.model;

/** Veículo da frota da locadora. */
public class Veiculo {

    private int id;
    private String placa;
    private String modelo;
    private int ano;
    private int quilometragem;
    private StatusVeiculo status;
    private Categoria categoria;

    /**
     * Cria um veículo novo, já disponível para locação.
     *
     * @param placa         placa do veículo
     * @param modelo        modelo (por exemplo, Fiat Strada)
     * @param ano           ano de fabricação
     * @param quilometragem quilometragem atual
     * @param categoria     categoria do veículo
     */
    public Veiculo(String placa, String modelo, int ano, int quilometragem, Categoria categoria) {
        this.placa = placa;
        this.modelo = modelo;
        this.ano = ano;
        this.quilometragem = quilometragem;
        this.categoria = categoria;
        this.status = StatusVeiculo.DISPONIVEL;
    }

    /**
     * Informa se o veículo pode ser entregue a um cliente.
     *
     * @return true se a situação é DISPONIVEL
     */
    public boolean estaDisponivel() {
        return status == StatusVeiculo.DISPONIVEL;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPlaca() {
        return placa;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public int getAno() {
        return ano;
    }

    public int getQuilometragem() {
        return quilometragem;
    }

    public void setQuilometragem(int quilometragem) {
        this.quilometragem = quilometragem;
    }

    public StatusVeiculo getStatus() {
        return status;
    }

    public void setStatus(StatusVeiculo status) {
        this.status = status;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    @Override
    public String toString() {
        return "[" + id + "] " + placa + " - " + modelo + " (" + ano + ") - " + quilometragem
                + " km - " + categoria.getNome() + " - " + status;
    }
}
