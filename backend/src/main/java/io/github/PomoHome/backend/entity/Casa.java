package io.github.PomoHome.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The player's house. Contains a fixed set of Slots, each of which can hold
 * (at most) one Movel placed by the player.
 */
@Entity
@Table(name = "CASA")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Casa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Inverse side of the Jogador.casa relationship.
     *
     * "mappedBy = casa" means: "the OTHER side (Jogador) owns the foreign
     * key column. Don't create another FK on the CASA table."
     */
    @OneToOne(mappedBy = "casa", fetch = FetchType.LAZY)
    @JsonIgnore
    private Jogador dono;

    @Column(nullable = false)
    private String nome;

    private int numLikes;

    /**
     * Ids of the players who have liked this house. A like is one-per-visitor
     * and toggleable (like / unlike), so {@code numLikes} is kept equal to this
     * set's size. Stored as a simple id collection (like Jogador.amigosIds) to
     * avoid a Casa↔Jogador cycle.
     */
    @ElementCollection
    @CollectionTable(name = "CASA_CURTIDO_POR", joinColumns = @JoinColumn(name = "casa_id"))
    @Column(name = "jogador_id")
    private Set<Long> curtidoPor = new HashSet<>();

    /**
     * Slots belong to this Casa and only this Casa.
     *  - cascade = ALL          -> persist/update/delete Slots with the Casa
     *  - orphanRemoval = true   -> if you remove a Slot from this list,
     *                              JPA also deletes it from the DB
     *  - mappedBy = "casa"      -> the FK lives on SLOT.casa_id; we add a
     *                              `Casa casa` field in Slot for this.
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

    public void addSlot(Slot s) {
        slots.add(s);
        s.setCasa(this);
    }

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

    public Set<Long> getCurtidoPor() { return curtidoPor; }
    public void setCurtidoPor(Set<Long> curtidoPor) { this.curtidoPor = curtidoPor; }
}
