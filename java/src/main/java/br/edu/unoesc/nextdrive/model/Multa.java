package br.edu.unoesc.nextdrive.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Multa de trânsito recebida depois da devolução e cobrada do cliente. */
public class Multa {
    private Integer id;
    private Integer locacaoId;
    private String descricao;
    private BigDecimal valor;
    private LocalDateTime dataInfracao;
    private boolean paga;

    public Multa() { }

    /**
     * @throws IllegalArgumentException se os dados não permitem registrar a multa
     */
    public void validar() {
        if (locacaoId == null) throw new IllegalArgumentException("Informe a locação.");
        if (descricao == null || descricao.isBlank()) throw new IllegalArgumentException("Informe a descrição da multa.");
        if (valor == null || valor.signum() <= 0) throw new IllegalArgumentException("Informe um valor maior que zero.");
        if (dataInfracao == null || dataInfracao.isAfter(LocalDateTime.now()))
            throw new IllegalArgumentException("Data da infração inválida.");
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getLocacaoId() { return locacaoId; }
    public void setLocacaoId(Integer l) { this.locacaoId = l; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String d) { this.descricao = d; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal v) { this.valor = v; }
    public LocalDateTime getDataInfracao() { return dataInfracao; }
    public void setDataInfracao(LocalDateTime d) { this.dataInfracao = d; }
    public boolean isPaga() { return paga; }
    public void setPaga(boolean paga) { this.paga = paga; }
}
