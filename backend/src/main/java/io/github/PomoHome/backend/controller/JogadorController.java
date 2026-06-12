package io.github.PomoHome.backend.controller;

import io.github.PomoHome.backend.dto.JogadorDTO;
import io.github.PomoHome.backend.dto.RankingEntryDTO;
import io.github.PomoHome.backend.service.JogadorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST endpoints under /api/jogadores.
 */
@RestController
@RequestMapping("/api/jogadores")
public class JogadorController {

    private final JogadorService jogadorService;

    public JogadorController(JogadorService jogadorService) {
        this.jogadorService = jogadorService;
    }

    /**
     * POST /api/jogadores
     * Body: { "username": "...", "senha": "..." }
     */
    @PostMapping
    public ResponseEntity<JogadorDTO> cadastrar(@RequestBody Map<String, String> body) {
        var novo = jogadorService.cadastrar(body.get("username"), body.get("senha"));
        return ResponseEntity.status(201).body(JogadorDTO.from(novo));
    }

    /**
     * POST /api/jogadores/login
     * Body: { "username": "...", "senha": "..." }
     */
    @PostMapping("/login")
    public ResponseEntity<JogadorDTO> login(@RequestBody Map<String, String> body) {
        var jogador = jogadorService.autenticar(body.get("username"), body.get("senha"));
        return ResponseEntity.ok(JogadorDTO.from(jogador));
    }

    /**
     * GET /api/jogadores/{id}
     * Used when a friend's profile / house is fetched.
     */
    @GetMapping("/{id}")
    public ResponseEntity<JogadorDTO> buscarPorId(@PathVariable Long id) {
        return jogadorService.buscarPorId(id)
                .map(JogadorDTO::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * GET /api/jogadores/username/{username}
     * Backs the "find a friend by username" search bar on the frontend.
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<JogadorDTO> buscarPorUsername(@PathVariable String username) {
        return jogadorService.buscarPorUsername(username)
                .map(JogadorDTO::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * POST /api/jogadores/{id}/creditar?valor=N  (admin/debug)
     * Adds N coins to the player's balance without changing study time.
     */
    @PostMapping("/{id}/creditar")
    public ResponseEntity<JogadorDTO> creditar(@PathVariable Long id, @RequestParam int valor) {
        return ResponseEntity.ok(JogadorDTO.from(jogadorService.creditarSaldo(id, valor)));
    }

    /**
     * DELETE /api/jogadores/{id}/inventario  (admin/debug)
     * Empties the player's furniture inventory.
     */
    @DeleteMapping("/{id}/inventario")
    public ResponseEntity<JogadorDTO> limparInventario(@PathVariable Long id) {
        return ResponseEntity.ok(JogadorDTO.from(jogadorService.limparInventario(id)));
    }

    /**
     * GET /api/jogadores/ranking
     * Returns all players sorted by tempoEstudado DESC.
     */
    @GetMapping("/ranking")
    public ResponseEntity<List<RankingEntryDTO>> ranking() {
        List<RankingEntryDTO> ranking = jogadorService.listarRanking().stream()
                .map(RankingEntryDTO::from)
                .toList();
        return ResponseEntity.ok(ranking);
    }
}
