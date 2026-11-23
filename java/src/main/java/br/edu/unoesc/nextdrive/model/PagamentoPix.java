package br.edu.unoesc.nextdrive.model;

import java.math.BigDecimal;

/** Pagamento por Pix: guarda o código da transação (txid). */
public class PagamentoPix extends Pagamento {

    private String txid;

    /**
     * Cria um pagamento por Pix.
     *
     * @param locacaoId código da locação paga
     * @param valor     valor pago
     * @param txid      código da transação Pix
     */
    public PagamentoPix(int locacaoId, BigDecimal valor, String txid) {
        super(locacaoId, valor);
        this.txid = txid;
    }

    public String getTxid() {
        return txid;
    }

    @Override
    public String getTipo() {
        return "PIX";
    }

    @Override
    public String descrever() {
        return "Pix (txid " + txid + ")";
    }
}
