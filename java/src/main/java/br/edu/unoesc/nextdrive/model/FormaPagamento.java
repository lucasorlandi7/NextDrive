package br.edu.unoesc.nextdrive.model;

/** Forma de pagamento aceita pela locadora (coluna pagamento.forma). */
public enum FormaPagamento {
    CARTAO_CREDITO("Cartão de crédito"), CARTAO_DEBITO("Cartão de débito"), PIX("PIX");

    private final String rotulo;

    FormaPagamento(String rotulo) { this.rotulo = rotulo; }

    /** @return o nome da forma de pagamento para exibição nas telas */
    public String getRotulo() { return rotulo; }

    /** @return true se o pagamento usa cartão (crédito ou débito) */
    public boolean usaCartao() { return this != PIX; }
}
