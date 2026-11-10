package br.edu.unoesc.nextdrive.model;

/**
 * Usuário autenticado no sistema (resultado de {@code fn_autenticar}).
 * Guarda apenas o necessário para a sessão: identificação e perfil.
 */
public final class Usuario {
    /** Perfil de quem não é funcionário. */
    public static final String CLIENTE = "CLIENTE";

    private final int id;
    private final String nome;
    private final String perfil;

    /**
     * @param id     código da pessoa
     * @param nome   nome para exibição
     * @param perfil CLIENTE, ATENDENTE ou GERENTE
     */
    public Usuario(int id, String nome, String perfil) {
        this.id = id;
        this.nome = nome;
        this.perfil = perfil;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getPerfil() { return perfil; }

    /** @return true se o usuário é funcionário (atendente ou gerente) */
    public boolean isFuncionario() { return !CLIENTE.equals(perfil); }

    /** @return true se o usuário é gerente */
    public boolean isGerente() { return Cargo.GERENTE.name().equals(perfil); }
}
