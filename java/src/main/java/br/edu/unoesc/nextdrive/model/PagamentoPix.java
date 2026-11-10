package br.edu.unoesc.nextdrive.model;

import java.math.BigDecimal;

/** Pagamento via PIX, identificado pelo txid da transação. */
public class PagamentoPix extends Pagamento {
    private String txid;
    private String chavePix;

    public PagamentoPix(Integer locacaoId, BigDecimal valor, String txid, String chavePix) {
        super(locacaoId, valor);
        this.txid = txid;
        this.chavePix = chavePix;
    }

    @Override
    public void validar() {
        super.validar();
        if (txid == null || txid.isBlank()) throw new IllegalArgumentException("Informe o txid do PIX.");
        if (txid.length() > 35) throw new IllegalArgumentException("O txid tem no máximo 35 caracteres.");
    }

    @Override
    public FormaPagamento getForma() { return FormaPagamento.PIX; }

    @Override
    public String descrever() { return "PIX (txid " + txid + ")"; }

    public String getTxid() { return txid; }
    public String getChavePix() { return chavePix; }
}
