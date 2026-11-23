package br.edu.unoesc.nextdrive.model;

import java.math.BigDecimal;

/** Pagamento com cartão: guarda a bandeira. */
public class PagamentoCartao extends Pagamento {

    private String bandeira;

    /**
     * Cria um pagamento com cartão.
     *
     * @param locacaoId código da locação paga
     * @param valor     valor pago
     * @param bandeira  bandeira do cartão (Visa, Mastercard...)
     */
    public PagamentoCartao(int locacaoId, BigDecimal valor, String bandeira) {
        super(locacaoId, valor);
        this.bandeira = bandeira;
    }

    public String getBandeira() {
        return bandeira;
    }

    @Override
    public String getTipo() {
        return "CARTAO";
    }

    @Override
    public String descrever() {
        return "Cartão " + bandeira;
    }
}
