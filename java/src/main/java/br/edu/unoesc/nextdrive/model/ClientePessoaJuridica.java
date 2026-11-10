package br.edu.unoesc.nextdrive.model;

/**
 * Cliente pessoa jurídica (empresa), identificado por CNPJ. Possui um condutor responsável,
 * pessoa física que é validada na retirada do veículo.
 */
public class ClientePessoaJuridica extends Cliente {
    private String cnpj;
    private String razaoSocial;
    private ClientePessoaFisica condutorResponsavel;

    public ClientePessoaJuridica() { }

    public ClientePessoaJuridica(Integer id, String nome, String email, String telefone,
                                 String cnpj, String razaoSocial, ClientePessoaFisica condutor) {
        super(id, nome, email, telefone);
        this.cnpj = cnpj;
        this.razaoSocial = razaoSocial;
        this.condutorResponsavel = condutor;
    }

    @Override
    public String getDocumento() { return cnpj; }

    @Override
    public ClientePessoaFisica getCondutor() { return condutorResponsavel; }

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }
    public String getRazaoSocial() { return razaoSocial; }
    public void setRazaoSocial(String razaoSocial) { this.razaoSocial = razaoSocial; }
    public ClientePessoaFisica getCondutorResponsavel() { return condutorResponsavel; }
    public void setCondutorResponsavel(ClientePessoaFisica c) { this.condutorResponsavel = c; }
}
