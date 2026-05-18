package io.github.PomoHome.backend.dto;

import io.github.PomoHome.backend.entity.SolicitacaoAmizade;
import io.github.PomoHome.backend.entity.StatusSolicitacao;

import java.time.LocalDateTime;

/**
 * Response shape for a friend request. Both players are flattened to ids,
 * so the client never receives the full Jogador graph through a request.
 */
public record SolicitacaoAmizadeDTO(Long id,
                                    Long remetenteId,
                                    Long destinatarioId,
                                    StatusSolicitacao status,
                                    LocalDateTime criadaEm) {

    public static SolicitacaoAmizadeDTO from(SolicitacaoAmizade s) {
        if (s == null) {
            return null;
        }
        return new SolicitacaoAmizadeDTO(
                s.getId(),
                s.getRemetenteId(),
                s.getDestinatarioId(),
                s.getStatus(),
                s.getCriadaEm());
    }
}
