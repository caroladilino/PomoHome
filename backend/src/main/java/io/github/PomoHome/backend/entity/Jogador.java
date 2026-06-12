package io.github.PomoHome.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Player entity — the central aggregate of the game.
 *
 * Relationships at a glance:
 *   Jogador 1 --- 1 Casa       (each player has exactly one house)
 *   Jogador *-----* Movel      (inventory: many players can own many "copies" of a Movel)
 *   Jogador *--{ amigosIds }   (friends: stored as a flat list of IDs to avoid circular JSON)
 *
 * Note: {@code senha} is a BCrypt hash and is never serialized to clients —
 * responses go through JogadorDTO, which omits it entirely.
 */
@Entity
@Table(name = "JOGADOR")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Jogador {

    // ---------------------------------------------------------------
    // Identity
    // ---------------------------------------------------------------

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique login name; backed by a unique index. */
    @Column(nullable = false, unique = true)
    private String username;

    /** BCrypt hash of the password — hashed in JogadorService.cadastrar. */
    @Column(nullable = false)
    private String senha;

    // ---------------------------------------------------------------
    // Game state
    // ---------------------------------------------------------------

    /** Coin balance (1 coin == 1 minute of completed Pomodoro). */
    private int saldo;

    /** Total minutes ever studied. Used to compute the ranking. */
    private int tempoEstudado;

    // ---------------------------------------------------------------
    // Relationships
    // ---------------------------------------------------------------

    /**
     * Each player has exactly one house. Cascade ALL means: persist/delete the
     * Casa together with this Jogador. The FK column "casa_id" lives on the
     * JOGADOR table.
     */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "casa_id")
    private Casa casa;

    /**
     * Inventory of furniture this player has bought.
     * Many-to-many because the same Movel model (e.g. "Red Sofa") can belong
     * to many players, and a player owns many móveis. Backed by the join
     * table JOGADOR_INVENTARIO (jogador_id, movel_id).
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "JOGADOR_INVENTARIO",
        joinColumns = @JoinColumn(name = "jogador_id"),
        inverseJoinColumns = @JoinColumn(name = "movel_id")
    )
    private List<Movel> inventario = new ArrayList<>();

    /**
     * Friends list, stored as a flat collection of Jogador IDs.
     *
     * Why not @ManyToMany Jogador? Because a self-referencing @ManyToMany
     * creates ugly JSON cycles (A is friend of B, which has friend A, which ...).
     * Storing IDs avoids the cycle and is perfectly fine for our use case
     * (we just need to know "who are this user's friends?" and then GET them
     * one by one via /api/jogadores/{id}). Backed by the side table
     * JOGADOR_AMIGOS_IDS; LAZY, so access it inside a @Transactional method.
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "JOGADOR_AMIGOS_IDS", joinColumns = @JoinColumn(name = "jogador_id"))
    @Column(name = "amigo_id")
    private List<Long> amigosIds = new ArrayList<>();

    // ---------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------

    /** Required no-arg constructor for JPA. Do not delete. */
    public Jogador() { }

    public Jogador(String username, String senha) {
        // New players start with no coins and no study time.
        this.username = username;
        this.senha = senha;
        this.saldo = 0;
        this.tempoEstudado = 0;
    }

    // ---------------------------------------------------------------
    // Getters & setters
    // ---------------------------------------------------------------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public int getSaldo() { return saldo; }
    public void setSaldo(int saldo) { this.saldo = saldo; }

    public int getTempoEstudado() { return tempoEstudado; }
    public void setTempoEstudado(int tempoEstudado) { this.tempoEstudado = tempoEstudado; }

    public Casa getCasa() { return casa; }
    public void setCasa(Casa casa) { this.casa = casa; }

    public List<Movel> getInventario() { return inventario; }
    public void setInventario(List<Movel> inventario) { this.inventario = inventario; }

    public List<Long> getAmigosIds() { return amigosIds; }
    public void setAmigosIds(List<Long> amigosIds) { this.amigosIds = amigosIds; }
}
