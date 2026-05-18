package io.github.PomoHome.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

/**
 * A piece of furniture that can be bought from the global store (Loja)
 * and placed in the player's house (Casa) on a compatible Slot.
 *
 * <p>Note (per the design brief): the original "ItemLoja" idea was MERGED
 * into Movel — i.e. a Movel IS the storefront item. There is no separate
 * Loja entity in the database; the "Loja" is simply "SELECT * FROM MOVEL".
 */
@Entity
@Table(name = "MOVEL")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Movel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    /** e.g. "sofa", "mesa", "cama" — drives the Slot compatibility check. */
    @Column(nullable = false)
    private String categoria;

    /** Cost in coins (1 coin == 1 minute studied). */
    @Column(nullable = false)
    private int preco;

    public Movel() { }

    public Movel(String nome, String categoria, int preco) {
        this.nome = nome;
        this.categoria = categoria;
        this.preco = preco;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public int getPreco() { return preco; }
    public void setPreco(int preco) { this.preco = preco; }
}
