package br.edu.unoesc.nextdrive.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Reserva de um veículo de uma categoria, feita pelo site ou no balcão. */
public class Reserva {
    private Integer id;
    private Integer clienteId;
    private Integer categoriaId;
    private Integer lojaRetiradaId;
    private Integer lojaDevolucaoId;
    private LocalDateTime dataRetiradaPrevista;
    private LocalDateTime dataDevolucaoPrevista;
    private BigDecimal valorEstimado;
    private Canal canal;
    private Integer funcionarioId;
    private StatusReserva status = StatusReserva.ATIVA;
    private LocalDateTime dataCancelamento;
    private String justificativaCancelamento;

    public Reserva() { }

    /**
     * Cancela a reserva (RF08). A justificativa é obrigatória e só reservas ativas podem ser canceladas.
     *
     * @param justificativa motivo do cancelamento
     * @throws IllegalStateException    se a reserva não está ativa
     * @throws IllegalArgumentException se a justificativa está vazia
     */
    public void cancelar(String justificativa) {
        if (status != StatusReserva.ATIVA) throw new IllegalStateException("Só reservas ativas podem ser canceladas.");
        if (justificativa == null || justificativa.isBlank())
            throw new IllegalArgumentException("Informe a justificativa do cancelamento.");
        this.status = StatusReserva.CANCELADA;
        this.justificativaCancelamento = justificativa.trim();
        this.dataCancelamento = LocalDateTime.now();
    }

    /**
     * Valida o período e as lojas antes de gravar (RN06).
     *
     * @throws IllegalArgumentException se algum dado obrigatório falta ou o período é inválido
     */
    public void validar() {
        if (clienteId == null || categoriaId == null || lojaRetiradaId == null || lojaDevolucaoId == null
                || dataRetiradaPrevista == null || dataDevolucaoPrevista == null)
            throw new IllegalArgumentException("Preencha todos os campos da reserva.");
        if (!dataDevolucaoPrevista.isAfter(dataRetiradaPrevista))
            throw new IllegalArgumentException("A devolução deve ser depois da retirada.");
        if (dataRetiradaPrevista.isBefore(LocalDateTime.now().minusMinutes(5)))
            throw new IllegalArgumentException("A retirada não pode ser no passado.");
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getClienteId() { return clienteId; }
    public void setClienteId(Integer c) { this.clienteId = c; }
    public Integer getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Integer c) { this.categoriaId = c; }
    public Integer getLojaRetiradaId() { return lojaRetiradaId; }
    public void setLojaRetiradaId(Integer l) { this.lojaRetiradaId = l; }
    public Integer getLojaDevolucaoId() { return lojaDevolucaoId; }
    public void setLojaDevolucaoId(Integer l) { this.lojaDevolucaoId = l; }
    public LocalDateTime getDataRetiradaPrevista() { return dataRetiradaPrevista; }
    public void setDataRetiradaPrevista(LocalDateTime d) { this.dataRetiradaPrevista = d; }
    public LocalDateTime getDataDevolucaoPrevista() { return dataDevolucaoPrevista; }
    public void setDataDevolucaoPrevista(LocalDateTime d) { this.dataDevolucaoPrevista = d; }
    public BigDecimal getValorEstimado() { return valorEstimado; }
    public void setValorEstimado(BigDecimal v) { this.valorEstimado = v; }
    public Canal getCanal() { return canal; }
    public void setCanal(Canal canal) { this.canal = canal; }
    public Integer getFuncionarioId() { return funcionarioId; }
    public void setFuncionarioId(Integer f) { this.funcionarioId = f; }
    public StatusReserva getStatus() { return status; }
    public void setStatus(StatusReserva s) { this.status = s; }
    public LocalDateTime getDataCancelamento() { return dataCancelamento; }
    public String getJustificativaCancelamento() { return justificativaCancelamento; }
}
