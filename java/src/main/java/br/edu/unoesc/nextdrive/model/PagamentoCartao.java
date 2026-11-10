package br.edu.unoesc.nextdrive.model;

import java.math.BigDecimal;

/** Pagamento em cartão de crédito (com parcelas) ou de débito (sempre à vista). */
public class PagamentoCartao extends Pagamento {
    private final FormaPagamento forma;
    private String bandeira;
    private String finalCartao;
    private int parcelas = 1;

    /**
     * @param forma CARTAO_CREDITO ou CARTAO_DEBITO
     * @throws IllegalArgumentException se a forma não for de cartão
     */
    public PagamentoCartao(Integer locacaoId, BigDecimal valor, FormaPagamento forma,
                           String bandeira, String finalCartao, int parcelas) {
        super(locacaoId, valor);
        if (!forma.usaCartao()) throw new IllegalArgumentException("Forma de pagamento não é de cartão.");
        this.forma = forma;
        this.bandeira = bandeira;
        this.finalCartao = finalCartao;
        this.parcelas = parcelas;
    }

    @Override
    public void validar() {
        super.validar();
        if (bandeira == null || bandeira.isBlank()) throw new IllegalArgumentException("Informe a bandeira do cartão.");
        if (finalCartao == null || !finalCartao.matches("\\d{4}"))
            throw new IllegalArgumentException("Informe os 4 últimos dígitos do cartão.");
        if (parcelas < 1 || parcelas > 12) throw new IllegalArgumentException("Parcelas devem ficar entre 1 e 12.");
        if (forma == FormaPagamento.CARTAO_DEBITO && parcelas != 1)
            throw new IllegalArgumentException("Débito é sempre à vista.");
    }

    @Override
    public FormaPagamento getForma() { return forma; }

    @Override
    public String descrever() {
        return forma.getRotulo() + " " + bandeira + " final " + finalCartao + " em " + parcelas + "x";
    }

    public String getBandeira() { return bandeira; }
    public String getFinalCartao() { return finalCartao; }
    public int getParcelas() { return parcelas; }
}
