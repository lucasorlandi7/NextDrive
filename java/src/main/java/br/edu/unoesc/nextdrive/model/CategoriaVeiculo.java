package br.edu.unoesc.nextdrive.model;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

/** Categoria de veículos, com os preços e taxas aplicados às locações. */
public class CategoriaVeiculo {
    private Integer id;
    private String nome;
    private String descricao;
    private BigDecimal valorDiaria;
    private BigDecimal valorHoraExtra;
    private BigDecimal taxaDevolucaoOutraLoja;
    private BigDecimal valorCautela;

    public CategoriaVeiculo() { }

    /**
     * Estima o valor da locação (RF07): cada período de 24 horas, ou fração, custa uma diária
     * (mínimo de uma diária). O valor final real é calculado na devolução pelo banco (RN06 a RN08).
     *
     * @param inicio retirada prevista
     * @param fim    devolução prevista
     * @return valor estimado em reais
     * @throws IllegalArgumentException se o fim não for posterior ao início
     */
    public BigDecimal calcularEstimativa(LocalDateTime inicio, LocalDateTime fim) {
        if (!fim.isAfter(inicio)) throw new IllegalArgumentException("A devolução deve ser depois da retirada.");
        long minutos = Duration.between(inicio, fim).toMinutes();
        long diarias = Math.max(1, (minutos + 1439) / 1440);
        return valorDiaria.multiply(BigDecimal.valueOf(diarias));
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public BigDecimal getValorDiaria() { return valorDiaria; }
    public void setValorDiaria(BigDecimal v) { this.valorDiaria = v; }
    public BigDecimal getValorHoraExtra() { return valorHoraExtra; }
    public void setValorHoraExtra(BigDecimal v) { this.valorHoraExtra = v; }
    public BigDecimal getTaxaDevolucaoOutraLoja() { return taxaDevolucaoOutraLoja; }
    public void setTaxaDevolucaoOutraLoja(BigDecimal v) { this.taxaDevolucaoOutraLoja = v; }
    public BigDecimal getValorCautela() { return valorCautela; }
    public void setValorCautela(BigDecimal v) { this.valorCautela = v; }
}
