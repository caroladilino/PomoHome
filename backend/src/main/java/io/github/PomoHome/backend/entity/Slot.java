package io.github.PomoHome.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

/**
 * A fixed position inside a Casa where the player can place ONE Movel.
 *
 * A slot is only compatible with móveis whose `categoria` equals
 * this slot's `categoriaPermitida`. So a "sofa" slot only accepts sofas.
 * The compatibility check lives in CasaService.colocarMovelNoSlot, not here —
 * entities stay free of business logic.
 */
@Entity
@Table(name = "SLOT")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Slot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Back-reference to the house this slot belongs to.
     * @JsonIgnore prevents the cycle Casa -> slots -> casa -> slots -> ...
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "casa_id")
    @JsonIgnore
    private Casa casa;

    /** Room/position id (e.g. "sala-canto-esquerdo") used by the client to render the móvel. */
    @Column(nullable = false)
    private String nomePosicao;

    /** Must match Movel.categoria exactly for placement to be allowed. */
    @Column(nullable = false)
    private String categoriaPermitida;

    /**
     * The móvel currently placed here, or null if the slot is empty.
     *
     * NOTE: this is @ManyToOne (not @OneToOne) because a Movel record in
     * the global catalog can be referenced by many Slots across many houses
     * — every player who buys a "Red Sofa" points at the same Movel row.
     * (If you want each placed móvel to be a unique instance with its own
     * state, you'd need a separate "MovelColocado" entity.)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movel_atual_id")
    private Movel movelAtual;

    public Slot() { }

    public Slot(String nomePosicao, String categoriaPermitida) {
        this.nomePosicao = nomePosicao;
        this.categoriaPermitida = categoriaPermitida;
        this.movelAtual = null;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Casa getCasa() { return casa; }
    public void setCasa(Casa casa) { this.casa = casa; }

    public String getNomePosicao() { return nomePosicao; }
    public void setNomePosicao(String nomePosicao) { this.nomePosicao = nomePosicao; }

    public String getCategoriaPermitida() { return categoriaPermitida; }
    public void setCategoriaPermitida(String categoriaPermitida) { this.categoriaPermitida = categoriaPermitida; }

    public Movel getMovelAtual() { return movelAtual; }
    public void setMovelAtual(Movel movelAtual) { this.movelAtual = movelAtual; }
}
