package io.github.PomoHome.backend.dto;

import io.github.PomoHome.backend.entity.Slot;

/**
 * Response shape for a house slot. 'movelAtual' is null when the slot
 * is empty.
 */
public record SlotDTO(Long id,
                       String nomePosicao,
                       String categoriaPermitida,
                       MovelDTO movelAtual) {

    public static SlotDTO from(Slot s) {
        if (s == null) {
            return null;
        }
        return new SlotDTO(
                s.getId(),
                s.getNomePosicao(),
                s.getCategoriaPermitida(),
                MovelDTO.from(s.getMovelAtual()));
    }
}
