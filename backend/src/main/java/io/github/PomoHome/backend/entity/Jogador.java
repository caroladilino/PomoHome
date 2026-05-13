package io.github.PomoHome.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
 * TODO (TEAM) — checklist when filling this class in:
 *   1. JPA needs a public no-arg constructor (already provided below).
 *      Don't delete it, even if you add a parameterized one.
 *   2. equals()/hashCode(): if you override them, NEVER use 'id' alone,
 *      because the id is null until the entity is persisted. Either use
 *      'username' (it's unique) or use the JPA-friendly pattern of
 *      `getClass()` equality + null-safe id compare.
 *   3. Don't expose `senha` in JSON responses (use a DTO when you serialize
 *      a Jogador to the client). For now the field is included; once you
 *      add DTOs, annotate it with @JsonIgnore here OR strip it in the DTO.
 */
@Entity
@Table(name = "JOGADOR")
public class Jogador {

    // ---------------------------------------------------------------
    // Identity
    // ---------------------------------------------------------------

    // TODO: @GeneratedValue(strategy = IDENTITY) tells Hibernate to let the
    //       database generate the primary key (H2 uses AUTO_INCREMENT under the hood).
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO: nullable = false enforces NOT NULL at the schema level;
    //       unique = true creates a unique index so two players can't share a name.
    @Column(nullable = false, unique = true)
    private String username;

    // TODO: store a HASH here, not the plain password. Plan: when you implement
    //       JogadorService.cadastrar(), hash the incoming senha with BCrypt
    //       (add spring-boot-starter-security or org.mindrot:jbcrypt as a dep).
    //       For the first prototype it's OK to keep it as plain text — just
    //       don't ship it that way.
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
     *
     * TODO: Jogador is the OWNING side here (it has the @JoinColumn).
     *       The inverse side (Casa.dono) uses mappedBy = "casa".
     *       Without this, JPA would create TWO foreign keys instead of one.
     */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "casa_id")
    private Casa casa;

    /**
     * Inventory of furniture this player has bought.
     * Many-to-many because the same Movel model (e.g. "Red Sofa") can belong
     * to many players, and a player owns many móveis.
     *
     * TODO: This creates a join table JOGADOR_INVENTARIO (jogador_id, movel_id).
     *       If you later need duplicates of the same Movel (e.g. two red sofas
     *       in inventory), switch to a @OneToMany with an "ItemInventario"
     *       intermediary entity.
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
     * one by one via /api/jogadores/{id}).
     *
     * TODO: @ElementCollection creates a side table JOGADOR_AMIGOS_IDS
     *       with columns (jogador_id, amigos_ids). Fetch is LAZY by default —
     *       you must either be inside a @Transactional method or call
     *       getAmigosIds().size() while the session is open.
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
        // TODO: defaults — new players start with saldo=0 and tempoEstudado=0.
        //       Consider initializing a default Casa here too, so a new
        //       Jogador always has a place to put furniture.
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
