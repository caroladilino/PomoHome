package io.github.PomoHome.backend.dto;

import io.github.PomoHome.backend.entity.SessaoEstudo;

import java.time.LocalDateTime;

/**
 * Response shape for a study session. The owning Jogador is flattened to
 * jogadorId — the client already knows which player it asked about.
 */
public record SessaoEstudoDTO(Long id,
                              Long jogadorId,
                              LocalDateTime dataHora,
                              int minutosConcluidos) {

    public static SessaoEstudoDTO from(SessaoEstudo s) {
        if (s == null) {
            return null;
        }
        Long jogadorId = s.getJogador() != null ? s.getJogador().getId() : null;
        return new SessaoEstudoDTO(s.getId(), jogadorId, s.getDataHora(), s.getMinutosConcluidos());
    }
}
