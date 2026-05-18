package io.github.PomoHome.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Client-side mirror of the backend's Casa entity.
 *
 * NOTE: 'dono' is NOT included here — the backend serializes Casa with
 * @JsonIgnore on dono to avoid a JSON cycle, so the JSON has no such field.
 *
 * TODO (TEAM): if the UI needs to know "whose house am I visiting?", you
 * already have that info from the request that fetched this Casa (e.g.
 * GET /api/casas/jogador/{jogadorId}).
 */
public class Casa {
    private Long id;
    private String nome;
    private int numLikes;
    private List<Slot> slots = new ArrayList<>();

    public Casa() { }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public int getNumLikes() { return numLikes; }
    public void setNumLikes(int numLikes) { this.numLikes = numLikes; }

    public List<Slot> getSlots() { return slots; }
    public void setSlots(List<Slot> slots) { this.slots = slots; }
}
