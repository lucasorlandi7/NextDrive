package br.edu.unoesc.nextdrive.model;

/**
 * Cliente da locadora: pessoa física ({@link ClientePessoaFisica}) ou jurídica
 * ({@link ClientePessoaJuridica}). As subclasses definem o documento e quem dirige o veículo.
 */
public abstract class Cliente extends Pessoa {

    protected Cliente() { }

    protected Cliente(Integer id, String nome, String email, String telefone) {
        super(id, nome, email, telefone);
    }

    /** @return o documento do cliente: CPF (pessoa física) ou CNPJ (pessoa jurídica) */
    public abstract String getDocumento();

    /**
     * Polimorfismo: a pessoa física dirige o próprio veículo; na empresa, quem dirige é o
     * condutor responsável cadastrado (RF04).
     *
     * @return a pessoa física que será validada como condutor na retirada (RF16)
     */
    public abstract ClientePessoaFisica getCondutor();

    @Override
    public String getPerfil() { return Usuario.CLIENTE; }
}
