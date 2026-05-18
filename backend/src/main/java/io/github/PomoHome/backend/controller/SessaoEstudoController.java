package io.github.PomoHome.backend.controller;

import io.github.PomoHome.backend.entity.SessaoEstudo;
import io.github.PomoHome.backend.service.SessaoEstudoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST endpoints under /api/sessoes.
 *
 * Lifecycle reminder:
 *   1. LibGDX client runs the local Pomodoro timer.
 *   2. When the timer hits "complete", the client POSTs a session here.
 *   3. We persist the session AND give the player coins / bump tempoEstudado.
 */
@RestController
@RequestMapping("/api/sessoes")
public class SessaoEstudoController {

    private final SessaoEstudoService sessaoService;

    public SessaoEstudoController(SessaoEstudoService sessaoService) {
        this.sessaoService = sessaoService;
    }

    /**
     * POST /api/sessoes
     * Body: { "jogadorId": 1, "minutosConcluidos": 25 }
     *
     * TODO:
     *   1. Parse jogadorId + minutosConcluidos from body (or use a DTO).
     *   2. SessaoEstudo nova = sessaoService.registrarSessao(jogadorId, minutos);
     *   3. return ResponseEntity.status(HttpStatus.CREATED).body(nova);
     */
    @PostMapping
    public ResponseEntity<SessaoEstudo> registrar(@RequestBody Map<String, Object> body) {
        // TODO: implement.
        return ResponseEntity.status(501).build();
    }

    /**
     * GET /api/sessoes/jogador/{jogadorId}
     * Returns the player's history, most recent first.
     *
     * TODO: return ResponseEntity.ok(sessaoService.historicoDoJogador(jogadorId));
     */
    @GetMapping("/jogador/{jogadorId}")
    public ResponseEntity<List<SessaoEstudo>> historico(@PathVariable Long jogadorId) {
        // TODO: implement.
        return ResponseEntity.status(501).build();
    }
}
