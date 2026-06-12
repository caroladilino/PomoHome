package io.github.PomoHome.model;

/**
 * Client-side mirror of the backend's Movel entity. NO JPA annotations.
 *
 * IMPORTANT: the server-backed fields MUST match the backend's
 * (id, nome, categoria, preco). Gson uses reflection to populate them
 * from the JSON returned by the API. If you rename a field here you
 * must also rename it on the server.
 *
 * The {@code widthInTiles}/{@code heightInTiles} fields are <b>client-only</b>:
 * the backend {@code MovelDTO} has no size, so the JSON never sets them and
 * Gson leaves them at their derived defaults. Size is decided from the
 * categoria via {@link #tamanhoPara(String)} — see the multi-tile placement
 * on the isometric grid (CasaActor).
 */
public class Movel {
    private Long id;
    private String nome;
    private String categoria;
    private int preco;

    // Client-only — NOT sent by the server. Derived from categoria.
    private transient int widthInTiles = 1;
    private transient int heightInTiles = 1;

    public Movel() { }

    /** Footprint (width, height) in grid tiles for a given categoria. */
    public static int[] tamanhoPara(String categoria) {
        if (categoria == null) {
            return new int[]{1, 1};
        }
        switch (categoria.toLowerCase()) {
            case "cama":  return new int[]{2, 3};
            case "sofa":  return new int[]{2, 1};
            case "mesa":  return new int[]{2, 2};
            default:      return new int[]{1, 1}; // cadeira, tapete, planta, ...
        }
    }

    /** Apply the categoria-derived footprint to this móvel's tile fields. */
    public Movel resolverTamanho() {
        int[] t = tamanhoPara(categoria);
        this.widthInTiles = t[0];
        this.heightInTiles = t[1];
        return this;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public int getPreco() { return preco; }
    public void setPreco(int preco) { this.preco = preco; }

    public int getWidthInTiles() { return widthInTiles <= 0 ? 1 : widthInTiles; }
    public int getHeightInTiles() { return heightInTiles <= 0 ? 1 : heightInTiles; }
}
