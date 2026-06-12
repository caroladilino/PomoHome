package io.github.PomoHome.model;

/**
 * One placed móvel in the layout-save payload sent to
 * {@code PUT /api/casas/{id}/layout}: which grid tile (the anchor) holds which
 * catalog móvel. Field names match the backend {@code CasaService.Placement}
 * record ({@code tileName}, {@code movelId}) — Gson serializes a
 * {@code List<Placement>} straight to the request body.
 */
public class Placement {
    private String tileName;
    private Long movelId;

    public Placement() { }

    public Placement(String tileName, Long movelId) {
        this.tileName = tileName;
        this.movelId = movelId;
    }

    public String getTileName() { return tileName; }
    public void setTileName(String tileName) { this.tileName = tileName; }

    public Long getMovelId() { return movelId; }
    public void setMovelId(Long movelId) { this.movelId = movelId; }
}
