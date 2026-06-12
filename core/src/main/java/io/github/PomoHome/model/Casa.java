package io.github.PomoHome.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Client-side mirror of the backend's Casa entity.
 *
 * NOTE: 'dono' is NOT included here — the backend serializes Casa with
 * @JsonIgnore on dono to avoid a JSON cycle, so the JSON has no such field.
 *
 * <p>Beyond the server fields (id, nome, numLikes, slots), this holds the
 * client's <b>placement map</b> for the free 8×8 isometric grid: anchor tile
 * name ("L{row}C{col}") → the móvel occupying it. {@link #fromSlots()} rebuilds
 * the map from the server's {@code slots} on load, and {@link #toPlacements()}
 * serializes it for {@code PUT /api/casas/{id}/layout} when edit mode ends.
 */
public class Casa {
    private Long id;
    private String nome;
    private int numLikes;
    private List<Long> curtidoPor = new ArrayList<>();
    private List<Slot> slots = new ArrayList<>();

    /** Client-only: anchor tile name -> placed móvel. Not serialized to JSON by name. */
    private transient final Map<String, Movel> placements = new LinkedHashMap<>();

    public Casa() { }

    // ---------------------------------------------------------------
    // Placement map (client-side grid state)
    // ---------------------------------------------------------------

    /** Rebuild the placement map from the server-provided slots. */
    public void fromSlots() {
        placements.clear();
        if (slots == null) {
            return;
        }
        for (Slot s : slots) {
            if (s != null && s.getNomePosicao() != null && s.getMovelAtual() != null) {
                placements.put(s.getNomePosicao(), s.getMovelAtual().resolverTamanho());
            }
        }
    }

    public void colocar(String tileName, Movel movel) {
        placements.put(tileName, movel);
    }

    public void removerTile(String tileName) {
        placements.remove(tileName);
    }

    /** Remove every tile that points at this móvel instance (anchor + footprint). */
    public void removerMovel(Movel movel) {
        placements.values().removeIf(m -> m == movel);
    }

    public void limparPlacements() {
        placements.clear();
    }

    public Map<String, Movel> getPlacements() {
        return placements;
    }

    /** Serialize the placement map to the layout-save body. */
    public List<Placement> toPlacements() {
        List<Placement> out = new ArrayList<>();
        for (Map.Entry<String, Movel> e : placements.entrySet()) {
            if (e.getValue() != null && e.getValue().getId() != null) {
                out.add(new Placement(e.getKey(), e.getValue().getId()));
            }
        }
        return out;
    }

    // ---------------------------------------------------------------
    // Server-mirror fields
    // ---------------------------------------------------------------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public int getNumLikes() { return numLikes; }
    public void setNumLikes(int numLikes) { this.numLikes = numLikes; }

    /** Ids of players who have liked this house (so the client can show like/unlike). */
    public List<Long> getCurtidoPor() { return curtidoPor; }
    public void setCurtidoPor(List<Long> curtidoPor) { this.curtidoPor = curtidoPor; }

    public List<Slot> getSlots() { return slots; }
    public void setSlots(List<Slot> slots) { this.slots = slots; }
}
