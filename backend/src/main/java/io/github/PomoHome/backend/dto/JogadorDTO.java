package io.github.PomoHome.backend.dto;

import io.github.PomoHome.backend.entity.Jogador;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Full profile of a player as returned to the client.
 *
 * CRITICAL: there is NO 'senha' field here. That is the whole reason this
 * DTO exists — the BCrypt hash must never leave the server.
 *
 * 'inventario' is included so TelaLoja can tell which móveis were already
 * bought. 'casa' is the nested CasaDTO (its slots carry placed furniture).
 */
public record JogadorDTO(Long id,
                          String username,
                          int saldo,
                          int tempoEstudado,
                          List<MovelDTO> inventario,
                          List<Long> amigosIds,
                          CasaDTO casa) {

    public static JogadorDTO from(Jogador j) {
        if (j == null) {
            return null;
        }
        List<MovelDTO> inventario = j.getInventario().stream()
                .map(MovelDTO::from)
                .collect(Collectors.toList());
        // Defensive copy: detach from the Hibernate-managed collection.
        List<Long> amigosIds = new ArrayList<>(j.getAmigosIds());
        return new JogadorDTO(
                j.getId(),
                j.getUsername(),
                j.getSaldo(),
                j.getTempoEstudado(),
                inventario,
                amigosIds,
                CasaDTO.from(j.getCasa()));
    }
}
