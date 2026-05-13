package io.github.PomoHome.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The player's house. Contains a fixed set of Slots, each of which can hold
 * (at most) one Movel placed by the player.
 *
 * TODO (TEAM):
 *   - When a Jogador is created (JogadorService.cadastrar), also create a
 *     fresh Casa with a default set of empty Slots (e.g. one "sala-sofa",
 *     one "sala-mesa", one "quarto-cama"). The catalog of slots is part of
 *     the game design — settle it with the team.
 *   - The bidirectional relation Jogador<->Casa is delicate: see comments
 *     on `dono` below.
 */
@Entity
@Table(name = "CASA")
public class Casa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Inverse side of the Jogador.casa relationship.
     *
     * "mappedBy = casa" means: "the OTHER side (Jogador) owns the foreign
     * key column. Don't create another FK on the CASA table."
     *
     * @JsonIgnore is CRITICAL: without it, serializing a Jogador will go
     *   Jogador -> casa -> dono -> Jogador -> ... infinite loop, and Jackson
     *   throws StackOverflowError. With @JsonIgnore, the client never sees
     *   `casa.dono` (which it already knows about — it's the logged-in user).
     */
    @OneToOne(mappedBy = "casa", fetch = FetchType.LAZY)
    @JsonIgnore
    private Jogador dono;

    @Column(nullable = false)
    private String nome;

    private int numLikes;

    /**
     * Slots belong to this Casa and only this Casa.
     *  - cascade = ALL          -> persist/update/delete Slots with the Casa
     *  - orphanRemoval = true   -> if you remove a Slot from this list,
     *                              JPA also deletes it from the DB
     *  - mappedBy = "casa"      -> the FK lives on SLOT.casa_id; we add a
     *                              `Casa casa` field in Slot for this.
     *
     * TODO: if you don't want the bidirectional reference on Slot, drop
     *       `mappedBy` and add @JoinColumn(name = "casa_id") here instead.
     *       Pick one style and be consistent.
     */
    @OneToMany(mappedBy = "casa", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Slot> slots = new ArrayList<>();

    public Casa() { }

    public Casa(String nome) {
        this.nome = nome;
        this.numLikes = 0;
    }

    // ---------------------------------------------------------------
    // Helpers (suggested) — keep both sides of the relationship in sync
    // ---------------------------------------------------------------

    /** TODO: call this instead of slots.add(...) so the back-reference is set. */
    public void addSlot(Slot s) {
        slots.add(s);
        s.setCasa(this);
    }

    /** TODO: call this instead of slots.remove(...) so orphanRemoval triggers cleanly. */
    public void removeSlot(Slot s) {
        slots.remove(s);
        s.setCasa(null);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Jogador getDono() { return dono; }
    public void setDono(Jogador dono) { this.dono = dono; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public int getNumLikes() { return numLikes; }
    public void setNumLikes(int numLikes) { this.numLikes = numLikes; }

    public List<Slot> getSlots() { return slots; }
    public void setSlots(List<Slot> slots) { this.slots = slots; }
}
