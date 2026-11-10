package br.edu.unoesc.nextdrive.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Locação: retirada do veículo, devolução e valores cobrados. Os valores são calculados pela
 * procedure {@code sp_registrar_devolucao} e apenas lidos pelo sistema.
 */
public class Locacao {
    private Integer id;
    private Integer reservaId;
    private Integer veiculoId;
    private Integer condutorId;
    private LocalDateTime dataRetirada;
    private LocalDateTime dataDevolucaoPrevista;
    private LocalDateTime dataDevolucaoReal;
    private int kmSaida;
    private Integer kmChegada;
    private int combustivelSaida;
    private Integer combustivelChegada;
    private BigDecimal cautelaValor = BigDecimal.ZERO;
    private boolean cautelaLiberada;
    private BigDecimal valorDiariaContratada;
    private BigDecimal valorDiarias;
    private BigDecimal valorHorasExtras;
    private BigDecimal valorCombustivel;
    private BigDecimal taxaDevolucaoOutraLoja;
    private BigDecimal desconto;
    private BigDecimal valorFinal;
    private BigDecimal totalPago = BigDecimal.ZERO;
    private StatusLocacao status = StatusLocacao.ABERTA;

    public Locacao() { }

    /** @return quanto ainda falta pagar (zero enquanto a locação não foi encerrada) */
    public BigDecimal saldoDevedor() {
        return valorFinal == null ? BigDecimal.ZERO : valorFinal.subtract(totalPago);
    }

    /** @return o total já pago pelo cliente */
    public BigDecimal totalPago() { return totalPago; }

    /** @return a quilometragem rodada, ou 0 se o veículo ainda não foi devolvido */
    public int kmRodados() { return kmChegada == null ? 0 : kmChegada - kmSaida; }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getReservaId() { return reservaId; }
    public void setReservaId(Integer r) { this.reservaId = r; }
    public Integer getVeiculoId() { return veiculoId; }
    public void setVeiculoId(Integer v) { this.veiculoId = v; }
    public Integer getCondutorId() { return condutorId; }
    public void setCondutorId(Integer c) { this.condutorId = c; }
    public LocalDateTime getDataRetirada() { return dataRetirada; }
    public void setDataRetirada(LocalDateTime d) { this.dataRetirada = d; }
    public LocalDateTime getDataDevolucaoPrevista() { return dataDevolucaoPrevista; }
    public void setDataDevolucaoPrevista(LocalDateTime d) { this.dataDevolucaoPrevista = d; }
    public LocalDateTime getDataDevolucaoReal() { return dataDevolucaoReal; }
    public void setDataDevolucaoReal(LocalDateTime d) { this.dataDevolucaoReal = d; }
    public int getKmSaida() { return kmSaida; }
    public void setKmSaida(int k) { this.kmSaida = k; }
    public Integer getKmChegada() { return kmChegada; }
    public void setKmChegada(Integer k) { this.kmChegada = k; }
    public int getCombustivelSaida() { return combustivelSaida; }
    public void setCombustivelSaida(int c) { this.combustivelSaida = c; }
    public Integer getCombustivelChegada() { return combustivelChegada; }
    public void setCombustivelChegada(Integer c) { this.combustivelChegada = c; }
    public BigDecimal getCautelaValor() { return cautelaValor; }
    public void setCautelaValor(BigDecimal c) { this.cautelaValor = c; }
    public boolean isCautelaLiberada() { return cautelaLiberada; }
    public void setCautelaLiberada(boolean c) { this.cautelaLiberada = c; }
    public BigDecimal getValorDiariaContratada() { return valorDiariaContratada; }
    public void setValorDiariaContratada(BigDecimal v) { this.valorDiariaContratada = v; }
    public BigDecimal getValorDiarias() { return valorDiarias; }
    public void setValorDiarias(BigDecimal v) { this.valorDiarias = v; }
    public BigDecimal getValorHorasExtras() { return valorHorasExtras; }
    public void setValorHorasExtras(BigDecimal v) { this.valorHorasExtras = v; }
    public BigDecimal getValorCombustivel() { return valorCombustivel; }
    public void setValorCombustivel(BigDecimal v) { this.valorCombustivel = v; }
    public BigDecimal getTaxaDevolucaoOutraLoja() { return taxaDevolucaoOutraLoja; }
    public void setTaxaDevolucaoOutraLoja(BigDecimal v) { this.taxaDevolucaoOutraLoja = v; }
    public BigDecimal getDesconto() { return desconto; }
    public void setDesconto(BigDecimal d) { this.desconto = d; }
    public BigDecimal getValorFinal() { return valorFinal; }
    public void setValorFinal(BigDecimal v) { this.valorFinal = v; }
    public void setTotalPago(BigDecimal t) { this.totalPago = t; }
    public StatusLocacao getStatus() { return status; }
    public void setStatus(StatusLocacao s) { this.status = s; }
}
