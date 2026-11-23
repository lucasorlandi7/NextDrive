package br.edu.unoesc.nextdrive.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Reserva de um veículo de uma categoria, feita por um cliente para um período. */
public class Reserva {

    private int id;
    private Cliente cliente;
    private Categoria categoria;
    private LocalDate dataRetiradaPrevista;
    private LocalDate dataDevolucaoPrevista;
    private StatusReserva status;

    /**
     * Cria uma reserva nova, com situação ATIVA.
     *
     * @param cliente               quem está reservando
     * @param categoria             categoria desejada
     * @param dataRetiradaPrevista  dia previsto para retirar o veículo
     * @param dataDevolucaoPrevista dia previsto para devolver o veículo
     */
    public Reserva(Cliente cliente, Categoria categoria,
                   LocalDate dataRetiradaPrevista, LocalDate dataDevolucaoPrevista) {
        this.cliente = cliente;
        this.categoria = categoria;
        this.dataRetiradaPrevista = dataRetiradaPrevista;
        this.dataDevolucaoPrevista = dataDevolucaoPrevista;
        this.status = StatusReserva.ATIVA;
    }

    /**
     * Cancela a reserva. Só uma reserva ATIVA pode ser cancelada.
     *
     * @return true se cancelou, false se a reserva não estava ativa
     */
    public boolean cancelar() {
        if (status != StatusReserva.ATIVA) {
            return false;
        }
        status = StatusReserva.CANCELADA;
        return true;
    }

    /**
     * Calcula o valor estimado da reserva: diária da categoria x dias (mínimo de 1 dia).
     *
     * @return valor estimado
     */
    public BigDecimal estimativa() {
        long dias = ChronoUnit.DAYS.between(dataRetiradaPrevista, dataDevolucaoPrevista);
        if (dias < 1) {
            dias = 1;
        }
        return categoria.getValorDiaria().multiply(BigDecimal.valueOf(dias));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public LocalDate getDataRetiradaPrevista() {
        return dataRetiradaPrevista;
    }

    public LocalDate getDataDevolucaoPrevista() {
        return dataDevolucaoPrevista;
    }

    public StatusReserva getStatus() {
        return status;
    }

    public void setStatus(StatusReserva status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "[" + id + "] " + cliente.getNome() + " - " + categoria.getNome()
                + " - de " + dataRetiradaPrevista + " a " + dataDevolucaoPrevista + " - " + status;
    }
}
