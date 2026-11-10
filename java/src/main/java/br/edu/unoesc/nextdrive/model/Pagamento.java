package br.edu.unoesc.nextdrive.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Pagamento de uma locação. Classe abstrata: cada forma de pagamento ({@link PagamentoCartao},
 * {@link PagamentoPix}) guarda os seus próprios dados e descreve a si mesma.
 */
public abstract class Pagamento {
    private Integer id;
    private Integer locacaoId;
    private BigDecimal valor;
    private LocalDateTime dataPagamento;

    protected Pagamento() { }

    protected Pagamento(Integer locacaoId, BigDecimal valor) {
        this.locacaoId = locacaoId;
        this.valor = valor;
    }

    /** @return a forma de pagamento deste objeto */
    public abstract FormaPagamento getForma();

    /** @return texto curto para recibos, como "PIX (txid X)" */
    public abstract String descrever();

    /**
     * Valida os dados específicos da forma de pagamento.
     *
     * @throws IllegalArgumentException se faltar algum dado ou ele for inválido
     */
    public void validar() {
        if (valor == null || valor.signum() <= 0) throw new IllegalArgumentException("Informe um valor maior que zero.");
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getLocacaoId() { return locacaoId; }
    public void setLocacaoId(Integer l) { this.locacaoId = l; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal v) { this.valor = v; }
    public LocalDateTime getDataPagamento() { return dataPagamento; }
    public void setDataPagamento(LocalDateTime d) { this.dataPagamento = d; }
}
