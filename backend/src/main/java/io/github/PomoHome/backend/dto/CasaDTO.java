package io.github.PomoHome.backend.dto;

import io.github.PomoHome.backend.entity.Casa;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Response shape for a house. The owner is flattened to donoId/donoUsername
 * (NOT a nested JogadorDTO) so there is no Casa <-> Jogador cycle. {@code
 * curtidoPor} lets the client tell whether the current visitor has already
 * liked it (so the button can show like vs unlike).
 */
public record CasaDTO(Long id,
                       String nome,
                       int numLikes,
                       Long donoId,
                       String donoUsername,
                       List<Long> curtidoPor,
                       List<SlotDTO> slots) {

    public static CasaDTO from(Casa c) {
        if (c == null) {
            return null;
        }
        Long donoId = c.getDono() != null ? c.getDono().getId() : null;
        String donoUsername = c.getDono() != null ? c.getDono().getUsername() : null;
        List<Long> curtidoPor = new ArrayList<>(c.getCurtidoPor());
        List<SlotDTO> slots = c.getSlots().stream()
                .map(SlotDTO::from)
                .collect(Collectors.toList());
        return new CasaDTO(c.getId(), c.getNome(), c.getNumLikes(), donoId, donoUsername,
                curtidoPor, slots);
    }
}
