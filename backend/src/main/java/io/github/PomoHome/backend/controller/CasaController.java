package io.github.PomoHome.backend.controller;

import io.github.PomoHome.backend.entity.Casa;
import io.github.PomoHome.backend.service.CasaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST endpoints under /api/casas.
 *
 * Convention used below for slot operations:
 *   PUT    /api/casas/slot/{slotId}/movel/{movelId}   -> place a móvel
 *   DELETE /api/casas/slot/{slotId}/movel             -> empty the slot
 *
 * Other styles (e.g. one body-based endpoint) would also be acceptable —
 * stay consistent.
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
     *
     * TODO: casaService.buscarPorJogador(jogadorId)
     *         .map(ResponseEntity::ok)
     *         .orElseGet(() -> ResponseEntity.notFound().build());
     */
    @GetMapping("/jogador/{jogadorId}")
    public ResponseEntity<Casa> buscarPorJogador(@PathVariable Long jogadorId) {
        // TODO: implement.
        return ResponseEntity.status(501).build();
    }

    /**
     * PUT /api/casas/slot/{slotId}/movel/{movelId}
     * Place 'movelId' on slot 'slotId'. Categoria must match.
     *
     * TODO: return ResponseEntity.ok(casaService.colocarMovelNoSlot(slotId, movelId));
     */
    @PutMapping("/slot/{slotId}/movel/{movelId}")
    public ResponseEntity<Casa> colocarMovel(@PathVariable Long slotId, @PathVariable Long movelId) {
        // TODO: implement.
        return ResponseEntity.status(501).build();
    }

    /**
     * DELETE /api/casas/slot/{slotId}/movel
     * Empty the slot (the móvel goes back to the player's storage).
     *
     * TODO: return ResponseEntity.ok(casaService.removerMovelDoSlot(slotId));
     */
    @DeleteMapping("/slot/{slotId}/movel")
    public ResponseEntity<Casa> removerMovel(@PathVariable Long slotId) {
        // TODO: implement.
        return ResponseEntity.status(501).build();
    }

    /**
     * POST /api/casas/{id}/like
     * A visiting friend likes the house.
     *
     * TODO: return ResponseEntity.ok(casaService.darLike(id));
     */
    @PostMapping("/{id}/like")
    public ResponseEntity<Casa> darLike(@PathVariable Long id) {
        // TODO: implement.
        return ResponseEntity.status(501).build();
    }
}
