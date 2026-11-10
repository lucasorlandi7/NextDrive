package br.edu.unoesc.nextdrive.model;

import java.time.LocalDate;
import java.time.Period;

/**
 * Cliente pessoa física, identificado por CPF e habilitado por uma CNH.
 * Concentra as regras de idade e habilitação usadas na retirada do veículo (RN05).
 */
public class ClientePessoaFisica extends Cliente {
    /** Idade mínima do condutor, em anos (RN05). */
    public static final int IDADE_MINIMA = 21;
    /** Tempo mínimo de habilitação, em anos (RN05). */
    public static final int HABILITACAO_MINIMA_ANOS = 2;

    private String cpf;
    private LocalDate dataNascimento;
    private String cnhNumero;
    private LocalDate cnhDataEmissao;
    private LocalDate cnhValidade;

    public ClientePessoaFisica() { }

    public ClientePessoaFisica(Integer id, String nome, String email, String telefone, String cpf,
                               LocalDate dataNascimento, String cnhNumero,
                               LocalDate cnhDataEmissao, LocalDate cnhValidade) {
        super(id, nome, email, telefone);
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
        this.cnhNumero = cnhNumero;
        this.cnhDataEmissao = cnhDataEmissao;
        this.cnhValidade = cnhValidade;
    }

    /**
     * @param data data de referência
     * @return a idade completa em anos na data informada
     */
    public int getIdade(LocalDate data) { return Period.between(dataNascimento, data).getYears(); }

    /**
     * @param data data de referência
     * @return true se a CNH ainda está dentro da validade na data informada
     */
    public boolean cnhValidaEm(LocalDate data) { return !cnhValidade.isBefore(data); }

    /**
     * @param anos quantidade mínima de anos
     * @param data data de referência
     * @return true se a CNH foi emitida há pelo menos {@code anos} anos
     */
    public boolean habilitadoHaAnos(int anos, LocalDate data) {
        return !cnhDataEmissao.plusYears(anos).isAfter(data);
    }

    /**
     * Confere a regra RN05 sem consultar o banco (usada para avisar o atendente antes da retirada).
     *
     * @param data data da retirada
     * @return a mensagem do problema encontrado ou {@code null} se o condutor está apto
     */
    public String validarCondutor(LocalDate data) {
        if (getIdade(data) < IDADE_MINIMA) return "Condutor com menos de " + IDADE_MINIMA + " anos.";
        if (!cnhValidaEm(data)) return "CNH vencida.";
        if (!habilitadoHaAnos(HABILITACAO_MINIMA_ANOS, data))
            return "CNH com menos de " + HABILITACAO_MINIMA_ANOS + " anos de habilitação.";
        return null;
    }

    @Override
    public String getDocumento() { return cpf; }

    @Override
    public ClientePessoaFisica getCondutor() { return this; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }
    public String getCnhNumero() { return cnhNumero; }
    public void setCnhNumero(String cnhNumero) { this.cnhNumero = cnhNumero; }
    public LocalDate getCnhDataEmissao() { return cnhDataEmissao; }
    public void setCnhDataEmissao(LocalDate cnhDataEmissao) { this.cnhDataEmissao = cnhDataEmissao; }
    public LocalDate getCnhValidade() { return cnhValidade; }
    public void setCnhValidade(LocalDate cnhValidade) { this.cnhValidade = cnhValidade; }
}
