package io.github.PomoHome.backend.dto;

import io.github.PomoHome.backend.entity.Movel;

/**
 * Response shape for a store/inventory item. A flat copy of the Movel
 * catalog row — no JPA, no proxies.
 */
public record MovelDTO(Long id, String nome, String categoria, int preco) {

    public static MovelDTO from(Movel m) {
        if (m == null) {
            return null;
        }
        return new MovelDTO(m.getId(), m.getNome(), m.getCategoria(), m.getPreco());
    }
}
