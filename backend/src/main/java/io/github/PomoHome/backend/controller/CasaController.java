package io.github.PomoHome.backend.controller;

import io.github.PomoHome.backend.dto.CasaDTO;
import io.github.PomoHome.backend.service.CasaService;
import io.github.PomoHome.backend.service.CasaService.LayoutRequest;
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
     * POST /api/casas/{id}/like?jogadorId={visitanteId}
     * A visiting friend toggles their like on the house (like / unlike).
     */
    @PostMapping("/{id}/like")
    public ResponseEntity<CasaDTO> darLike(@PathVariable Long id, @RequestParam Long jogadorId) {
        return ResponseEntity.ok(CasaDTO.from(casaService.darLike(id, jogadorId)));
    }

    /**
     * PUT /api/casas/{casaId}/layout
     * Body: { "nome": "Minha Casa",
     *         "placements": [ { "tileName": "L3C5", "movelId": 7 }, ... ] }.
     *
     * Replaces the whole house layout (the free 8×8 grid) AND persists the house
     * name. Called by the client when it leaves "edit mode". Returns the Casa.
     */
    @PutMapping("/{casaId}/layout")
    public ResponseEntity<CasaDTO> salvarLayout(@PathVariable Long casaId,
                                                @RequestBody LayoutRequest req) {
        return ResponseEntity.ok(CasaDTO.from(
                casaService.salvarLayout(casaId, req.nome(), req.placements())));
    }
}
