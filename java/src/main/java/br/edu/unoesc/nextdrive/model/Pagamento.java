package br.edu.unoesc.nextdrive.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Pagamento de uma locação. Classe abstrata: todo pagamento é por cartão
 * ({@link PagamentoCartao}) ou por Pix ({@link PagamentoPix}).
 */
public abstract class Pagamento {

    private int id;
    private int locacaoId;
    private BigDecimal valor;
    private LocalDateTime dataPagamento;

    /**
     * Cria um pagamento feito agora.
     *
     * @param locacaoId código da locação paga
     * @param valor     valor pago
     */
    protected Pagamento(int locacaoId, BigDecimal valor) {
        this.locacaoId = locacaoId;
        this.valor = valor;
        this.dataPagamento = LocalDateTime.now();
    }

    /**
     * Tipo do pagamento, igual ao que é gravado no banco.
     *
     * @return "CARTAO" ou "PIX"
     */
    public abstract String getTipo();

    /**
     * Descreve a forma de pagamento em texto. Cada tipo descreve do seu jeito.
     *
     * @return descrição da forma de pagamento
     */
    public abstract String descrever();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLocacaoId() {
        return locacaoId;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public LocalDateTime getDataPagamento() {
        return dataPagamento;
    }

    public void setDataPagamento(LocalDateTime dataPagamento) {
        this.dataPagamento = dataPagamento;
    }

    @Override
    public String toString() {
        return "[" + id + "] R$ " + valor + " - " + descrever() + " - "
                + dataPagamento.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}
