package br.edu.unoesc.nextdrive.model;

import java.math.BigDecimal;

/** Categoria de veículo (Econômico, SUV, Luxo). Define o valor da diária. */
public class Categoria {

    private int id;
    private String nome;
    private BigDecimal valorDiaria;

    /**
     * Cria uma categoria.
     *
     * @param id          código no banco
     * @param nome        nome da categoria
     * @param valorDiaria valor cobrado por dia de locação
     */
    public Categoria(int id, String nome, BigDecimal valorDiaria) {
        this.id = id;
        this.nome = nome;
        this.valorDiaria = valorDiaria;
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public BigDecimal getValorDiaria() {
        return valorDiaria;
    }

    @Override
    public String toString() {
        return "[" + id + "] " + nome + " - R$ " + valorDiaria + " por dia";
    }
}
