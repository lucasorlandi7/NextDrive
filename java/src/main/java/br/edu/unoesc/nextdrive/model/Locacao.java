package br.edu.unoesc.nextdrive.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Locação: o veículo foi entregue ao cliente a partir de uma reserva. */
public class Locacao {

    /** Multa por dia de atraso: 30% do valor da diária. */
    private static final BigDecimal MULTA_ATRASO = new BigDecimal("0.30");

    private int id;
    private Reserva reserva;
    private Veiculo veiculo;
    private LocalDate dataRetirada;
    private LocalDate dataDevolucaoReal;
    private int kmSaida;
    private Integer kmChegada;
    private BigDecimal valorFinal;
    private StatusLocacao status;

    /**
     * Abre uma locação (retirada do veículo).
     *
     * @param reserva      reserva que originou a locação
     * @param veiculo      veículo entregue
     * @param dataRetirada dia em que o cliente retirou o veículo
     */
    public Locacao(Reserva reserva, Veiculo veiculo, LocalDate dataRetirada) {
        this.reserva = reserva;
        this.veiculo = veiculo;
        this.dataRetirada = dataRetirada;
        this.kmSaida = veiculo.getQuilometragem();
        this.status = StatusLocacao.ABERTA;
    }

    /**
     * Calcula o valor final: diária x dias usados (mínimo de 1 dia) mais 30% da diária
     * por dia de atraso em relação à devolução prevista na reserva.
     *
     * @param devolucaoReal dia em que o veículo foi devolvido
     * @return valor final com 2 casas decimais
     */
    public BigDecimal calcularValorFinal(LocalDate devolucaoReal) {
        BigDecimal diaria = reserva.getCategoria().getValorDiaria();
        long dias = ChronoUnit.DAYS.between(dataRetirada, devolucaoReal);
        if (dias < 1) {
            dias = 1;
        }
        long atraso = ChronoUnit.DAYS.between(reserva.getDataDevolucaoPrevista(), devolucaoReal);
        if (atraso < 0) {
            atraso = 0;
        }
        BigDecimal valor = diaria.multiply(BigDecimal.valueOf(dias));
        BigDecimal multa = diaria.multiply(MULTA_ATRASO).multiply(BigDecimal.valueOf(atraso));
        return valor.add(multa).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Registra a devolução: guarda data e quilometragem, calcula o valor e encerra a locação.
     *
     * @param kmChegada     quilometragem do veículo na devolução
     * @param devolucaoReal dia da devolução
     */
    public void encerrar(int kmChegada, LocalDate devolucaoReal) {
        this.kmChegada = kmChegada;
        this.dataDevolucaoReal = devolucaoReal;
        this.valorFinal = calcularValorFinal(devolucaoReal);
        this.status = StatusLocacao.ENCERRADA;
    }

    /**
     * Calcula quanto ainda falta pagar.
     *
     * @param totalPago soma dos pagamentos já feitos
     * @return valor final menos o total pago (zero se a locação ainda não foi encerrada)
     */
    public BigDecimal saldoDevedor(BigDecimal totalPago) {
        if (valorFinal == null) {
            return BigDecimal.ZERO;
        }
        return valorFinal.subtract(totalPago);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Reserva getReserva() {
        return reserva;
    }

    public Veiculo getVeiculo() {
        return veiculo;
    }

    public LocalDate getDataRetirada() {
        return dataRetirada;
    }

    public LocalDate getDataDevolucaoReal() {
        return dataDevolucaoReal;
    }

    public void setDataDevolucaoReal(LocalDate dataDevolucaoReal) {
        this.dataDevolucaoReal = dataDevolucaoReal;
    }

    public int getKmSaida() {
        return kmSaida;
    }

    public void setKmSaida(int kmSaida) {
        this.kmSaida = kmSaida;
    }

    public Integer getKmChegada() {
        return kmChegada;
    }

    public void setKmChegada(Integer kmChegada) {
        this.kmChegada = kmChegada;
    }

    public BigDecimal getValorFinal() {
        return valorFinal;
    }

    public void setValorFinal(BigDecimal valorFinal) {
        this.valorFinal = valorFinal;
    }

    public StatusLocacao getStatus() {
        return status;
    }

    public void setStatus(StatusLocacao status) {
        this.status = status;
    }

    @Override
    public String toString() {
        String devolucao = dataDevolucaoReal == null ? "em aberto" : dataDevolucaoReal.toString();
        String valor = valorFinal == null ? "a calcular" : "R$ " + valorFinal;
        return "[" + id + "] " + reserva.getCliente().getNome() + " - " + veiculo.getPlaca()
                + " " + veiculo.getModelo() + " - retirada " + dataRetirada + " - devolução " + devolucao
                + " - " + valor + " - " + status;
    }
}
