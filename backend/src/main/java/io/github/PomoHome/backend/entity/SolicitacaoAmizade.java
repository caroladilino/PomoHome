package io.github.PomoHome.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * A friend request: 'remetente' asked 'destinatario' to become friends.
 *
 * The actual friendship still lives in Jogador.amigosIds — this entity only
 * models the PENDING / ACCEPTED / REJECTED handshake. When a request is
 * accepted, the service writes both ids into each player's amigosIds.
 *
 * The Jogador references are @JsonIgnore'd to avoid dragging each player's
 * whole graph (casa, inventário, ...) into the JSON. We expose just the ids
 * via getRemetenteId() / getDestinatarioId() so the client gets a flat:
 *   { id, remetenteId, destinatarioId, status, criadaEm }
 */
@Entity
@Table(name = "SOLICITACAO_AMIZADE")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SolicitacaoAmizade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "remetente_id", nullable = false)
    @JsonIgnore
    private Jogador remetente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destinatario_id", nullable = false)
    @JsonIgnore
    private Jogador destinatario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusSolicitacao status;

    @Column(nullable = false)
    private LocalDateTime criadaEm;

    public SolicitacaoAmizade() { }

    public SolicitacaoAmizade(Jogador remetente, Jogador destinatario) {
        this.remetente = remetente;
        this.destinatario = destinatario;
        this.status = StatusSolicitacao.PENDENTE;
        this.criadaEm = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Jogador getRemetente() { return remetente; }
    public void setRemetente(Jogador remetente) { this.remetente = remetente; }

    public Jogador getDestinatario() { return destinatario; }
    public void setDestinatario(Jogador destinatario) { this.destinatario = destinatario; }

    public StatusSolicitacao getStatus() { return status; }
    public void setStatus(StatusSolicitacao status) { this.status = status; }

    public LocalDateTime getCriadaEm() { return criadaEm; }
    public void setCriadaEm(LocalDateTime criadaEm) { this.criadaEm = criadaEm; }

    /** Flat id exposed in JSON instead of the full remetente graph. */
    @JsonProperty("remetenteId")
    public Long getRemetenteId() { return remetente != null ? remetente.getId() : null; }

    /** Flat id exposed in JSON instead of the full destinatario graph. */
    @JsonProperty("destinatarioId")
    public Long getDestinatarioId() { return destinatario != null ? destinatario.getId() : null; }
}
