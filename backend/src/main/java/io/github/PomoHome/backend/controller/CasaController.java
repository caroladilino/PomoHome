package io.github.PomoHome.backend.controller;

import io.github.PomoHome.backend.dto.CasaDTO;
import io.github.PomoHome.backend.service.CasaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST endpoints under /api/casas.
 *
 * Convention used below for slot operations:
 *   PUT    /api/casas/slot/{slotId}/movel/{movelId}   -> place a móvel
 *   DELETE /api/casas/slot/{slotId}/movel             -> empty the slot
 */
@RestController
@RequestMapping("/api/casas")
public class CasaController {

    private final CasaService casaService;

    public CasaController(CasaService casaService) {
        this.casaService = casaService;
    }

    /**
     * GET /api/casas/jogador/{jogadorId}
     * Returns the house of the given player, or 404.
     */
    @GetMapping("/jogador/{jogadorId}")
    public ResponseEntity<CasaDTO> buscarPorJogador(@PathVariable Long jogadorId) {
        return casaService.buscarPorJogador(jogadorId)
                .map(CasaDTO::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * PUT /api/casas/slot/{slotId}/movel/{movelId}
     * Place 'movelId' on slot 'slotId'. Categoria must match.
     */
    @PutMapping("/slot/{slotId}/movel/{movelId}")
    public ResponseEntity<CasaDTO> colocarMovel(@PathVariable Long slotId, @PathVariable Long movelId) {
        return ResponseEntity.ok(CasaDTO.from(casaService.colocarMovelNoSlot(slotId, movelId)));
    }

    /**
     * DELETE /api/casas/slot/{slotId}/movel
     * Empty the slot (the móvel goes back to the player's storage).
     */
    @DeleteMapping("/slot/{slotId}/movel")
    public ResponseEntity<CasaDTO> removerMovel(@PathVariable Long slotId) {
        return ResponseEntity.ok(CasaDTO.from(casaService.removerMovelDoSlot(slotId)));
    }

    /**
     * POST /api/casas/{id}/like
     * A visiting friend likes the house.
     */
    @PostMapping("/{id}/like")
    public ResponseEntity<CasaDTO> darLike(@PathVariable Long id) {
        return ResponseEntity.ok(CasaDTO.from(casaService.darLike(id)));
    }
}
