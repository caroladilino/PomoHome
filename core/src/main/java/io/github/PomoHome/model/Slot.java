package io.github.PomoHome.model;

/**
 * Client-side mirror of the backend's Slot entity.
 *
 * Each Slot belongs to a Casa and may hold ONE Movel whose categoria
 * equals categoriaPermitida.
 *
 * TODO (TEAM): when rendering the Casa screen, use 'nomePosicao' to look
 * up x/y coordinates from a config file (e.g. positions.json).
 */
public class Slot {
    private Long id;
    private String nomePosicao;
    private String categoriaPermitida;
    private Movel movelAtual;  // nullable -> empty slot

    public Slot() { }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNomePosicao() { return nomePosicao; }
    public void setNomePosicao(String nomePosicao) { this.nomePosicao = nomePosicao; }

    public String getCategoriaPermitida() { return categoriaPermitida; }
    public void setCategoriaPermitida(String categoriaPermitida) { this.categoriaPermitida = categoriaPermitida; }

    public Movel getMovelAtual() { return movelAtual; }
    public void setMovelAtual(Movel movelAtual) { this.movelAtual = movelAtual; }

    public boolean estaVazio() {
        return movelAtual == null;
    }
}
