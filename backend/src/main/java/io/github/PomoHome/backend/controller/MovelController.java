package io.github.PomoHome.backend.controller;

import io.github.PomoHome.backend.entity.Jogador;
import io.github.PomoHome.backend.entity.Movel;
import io.github.PomoHome.backend.service.JogadorService;
import io.github.PomoHome.backend.service.MovelService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST endpoints under /api/loja.
 *
 * The store IS the Movel table — we just expose it as "/loja" because that's
 * how the team talks about it in the design.
 */
@RestController
@RequestMapping("/api/loja")
public class MovelController {

    private final MovelService movelService;
    private final JogadorService jogadorService;

    public MovelController(MovelService movelService, JogadorService jogadorService) {
        this.movelService = movelService;
        this.jogadorService = jogadorService;
    }

    /**
     * GET /api/loja
     * Full catalog. The frontend calls this once to populate TelaLoja.
     *
     * TODO: return ResponseEntity.ok(movelService.listarTodos());
     */
    @GetMapping
    public ResponseEntity<List<Movel>> listarTodos() {
        // TODO: implement.
        return ResponseEntity.status(501).build();
    }

    /**
     * GET /api/loja/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Movel> buscarPorId(@PathVariable Long id) {
        // TODO: movelService.buscarPorId(id) -> ResponseEntity.ok | notFound.
        return ResponseEntity.status(501).build();
    }

    /**
     * POST /api/loja  (admin)
     * Body: a full Movel JSON.
     *
     * TODO: return ResponseEntity.status(HttpStatus.CREATED).body(movelService.criar(body));
     */
    @PostMapping
    public ResponseEntity<Movel> criar(@RequestBody Movel body) {
        // TODO: implement.
        return ResponseEntity.status(501).build();
    }

    /**
     * DELETE /api/loja/{id}  (admin)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        // TODO: movelService.removerPorId(id); return ResponseEntity.noContent().build();
        return ResponseEntity.status(501).build();
    }

    /**
     * POST /api/loja/comprar
     * Body: { "jogadorId": 1, "movelId": 7 }
     *
     * NB: we delegate to JogadorService here because the purchase mutates
     * Jogador (saldo + inventário), not Movel.
     *
     * TODO:
     *   1. Long jogadorId = Long.valueOf(body.get("jogadorId").toString());
     *   2. Long movelId   = Long.valueOf(body.get("movelId").toString());
     *   3. Jogador atualizado = jogadorService.comprarMovel(jogadorId, movelId);
     *   4. return ResponseEntity.ok(atualizado);
     *
     * Better: create a "ComprarRequest" DTO with two Long fields so Spring
     * binds the JSON for you with no casting.
     */
    @PostMapping("/comprar")
    public ResponseEntity<Jogador> comprar(@RequestBody Map<String, Object> body) {
        // TODO: implement.
        return ResponseEntity.status(501).build();
    }
}
