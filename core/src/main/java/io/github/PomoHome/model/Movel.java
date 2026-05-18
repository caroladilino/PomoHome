package io.github.PomoHome.model;

/**
 * Client-side mirror of the backend's Movel entity. NO JPA annotations.
 *
 * IMPORTANT: field names MUST match the backend's
 * (id, nome, categoria, preco). Gson uses reflection to populate them
 * from the JSON returned by the API. If you rename a field here you
 * must also rename it on the server.
 *
 * TODO (TEAM): public no-arg constructor + getters/setters are enough.
 * If you don't like the public getters/setters, you can keep fields
 * package-private — Gson reads private fields fine.
 */
public class Movel {
    private Long id;
    private String nome;
    private String categoria;
    private int preco;

    public Movel() { }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public int getPreco() { return preco; }
    public void setPreco(int preco) { this.preco = preco; }
}
