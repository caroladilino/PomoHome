package io.github.PomoHome.backend.dto;

import io.github.PomoHome.backend.entity.Jogador;

/**
 * Lightweight row for GET /api/jogadores/ranking.
 *
 * Deliberately NOT a full JogadorDTO: the ranking lists every player, and
 * we don't want to drag each one's casa + inventário over the wire (that
 * would also trigger an N+1 lazy load). Same data source, different shape —
 * exactly what DTOs are for.
 */
public record RankingEntryDTO(Long id, String username, int tempoEstudado) {

    public static RankingEntryDTO from(Jogador j) {
        return new RankingEntryDTO(j.getId(), j.getUsername(), j.getTempoEstudado());
    }
}
