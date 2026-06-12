package io.github.PomoHome.backend.controller;

import io.github.PomoHome.backend.dto.SessaoEstudoDTO;
import io.github.PomoHome.backend.service.SessaoEstudoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST endpoints under /api/sessoes.
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
     */
    @PostMapping
    public ResponseEntity<SessaoEstudoDTO> registrar(@RequestBody Map<String, Object> body) {
        Long jogadorId = Long.valueOf(body.get("jogadorId").toString());
        int minutosConcluidos = Integer.parseInt(body.get("minutosConcluidos").toString());
        var nova = sessaoService.registrarSessao(jogadorId, minutosConcluidos);
        return ResponseEntity.status(201).body(SessaoEstudoDTO.from(nova));
    }

    /**
     * GET /api/sessoes/jogador/{jogadorId}
     * Returns the player's history, most recent first.
     */
    @GetMapping("/jogador/{jogadorId}")
    public ResponseEntity<List<SessaoEstudoDTO>> historico(@PathVariable Long jogadorId) {
        List<SessaoEstudoDTO> historico = sessaoService.historicoDoJogador(jogadorId).stream()
                .map(SessaoEstudoDTO::from)
                .toList();
        return ResponseEntity.ok(historico);
    }
}
