package io.github.PomoHome.model;

/**
 * Client-side mirror of the backend's SolicitacaoAmizadeDTO (a friend request).
 *
 * Field names match the DTO exactly so Gson can map them:
 * {@code id, remetenteId, destinatarioId, status, criadaEm}. Both players are
 * just ids — resolve them to usernames with
 * {@code ApiClient.fetchJogadorPorId} when the UI needs to show a name.
 *
 * {@code status} is kept as a String (the server sends the enum name, e.g.
 * "PENDENTE", "ACEITA", "RECUSADA"); {@code criadaEm} as the ISO-8601 string.
 */
public class SolicitacaoAmizade {
    private Long id;
    private Long remetenteId;
    private Long destinatarioId;
    private String status;
    private String criadaEm;

    public SolicitacaoAmizade() { }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRemetenteId() { return remetenteId; }
    public void setRemetenteId(Long remetenteId) { this.remetenteId = remetenteId; }

    public Long getDestinatarioId() { return destinatarioId; }
    public void setDestinatarioId(Long destinatarioId) { this.destinatarioId = destinatarioId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCriadaEm() { return criadaEm; }
    public void setCriadaEm(String criadaEm) { this.criadaEm = criadaEm; }
}
