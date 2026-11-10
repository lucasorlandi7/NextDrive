package br.edu.unoesc.nextdrive.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Manutenção de um veículo. Enquanto está em andamento o veículo fica indisponível (RN04, RN11). */
public class Manutencao {
    private Integer id;
    private Integer veiculoId;
    private TipoManutencao tipo;
    private String descricao;
    private LocalDate dataEntrada;
    private LocalDate dataSaida;
    private BigDecimal custo;
    private boolean concluida;

    public Manutencao() { }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getVeiculoId() { return veiculoId; }
    public void setVeiculoId(Integer v) { this.veiculoId = v; }
    public TipoManutencao getTipo() { return tipo; }
    public void setTipo(TipoManutencao t) { this.tipo = t; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String d) { this.descricao = d; }
    public LocalDate getDataEntrada() { return dataEntrada; }
    public void setDataEntrada(LocalDate d) { this.dataEntrada = d; }
    public LocalDate getDataSaida() { return dataSaida; }
    public void setDataSaida(LocalDate d) { this.dataSaida = d; }
    public BigDecimal getCusto() { return custo; }
    public void setCusto(BigDecimal c) { this.custo = c; }
    public boolean isConcluida() { return concluida; }
    public void setConcluida(boolean c) { this.concluida = c; }
}
