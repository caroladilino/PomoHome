package io.github.PomoHome.backend.controller;

import io.github.PomoHome.backend.dto.SolicitacaoAmizadeDTO;
import io.github.PomoHome.backend.service.SolicitacaoAmizadeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints under /api/solicitacoes.
 *
 *   POST   /api/solicitacoes?remetenteId=1&destinatarioId=2  -> send a request
 *   POST   /api/solicitacoes/{id}/aceitar                    -> accept
 *   POST   /api/solicitacoes/{id}/recusar                    -> reject
 *   GET    /api/solicitacoes/recebidas/{jogadorId}           -> my inbox
 *   GET    /api/solicitacoes/enviadas/{jogadorId}            -> my outbox
 *
 * Thin controller: all rules live in SolicitacaoAmizadeService.
 */
@RestController
@RequestMapping("/api/solicitacoes")
public class SolicitacaoAmizadeController {

    private final SolicitacaoAmizadeService solicitacaoService;

    public SolicitacaoAmizadeController(SolicitacaoAmizadeService solicitacaoService) {
        this.solicitacaoService = solicitacaoService;
    }

    @PostMapping
    public ResponseEntity<SolicitacaoAmizadeDTO> enviar(@RequestParam Long remetenteId,
                                                        @RequestParam Long destinatarioId) {
        var s = solicitacaoService.enviarSolicitacao(remetenteId, destinatarioId);
        return ResponseEntity.status(201).body(SolicitacaoAmizadeDTO.from(s));
    }

    @PostMapping("/{id}/aceitar")
    public ResponseEntity<SolicitacaoAmizadeDTO> aceitar(@PathVariable Long id) {
        return ResponseEntity.ok(SolicitacaoAmizadeDTO.from(solicitacaoService.aceitar(id)));
    }

    @PostMapping("/{id}/recusar")
    public ResponseEntity<SolicitacaoAmizadeDTO> recusar(@PathVariable Long id) {
        return ResponseEntity.ok(SolicitacaoAmizadeDTO.from(solicitacaoService.recusar(id)));
    }

    @GetMapping("/recebidas/{jogadorId}")
    public ResponseEntity<List<SolicitacaoAmizadeDTO>> recebidas(@PathVariable Long jogadorId) {
        List<SolicitacaoAmizadeDTO> recebidas = solicitacaoService.listarRecebidasPendentes(jogadorId)
                .stream().map(SolicitacaoAmizadeDTO::from).toList();
        return ResponseEntity.ok(recebidas);
    }

    @GetMapping("/enviadas/{jogadorId}")
    public ResponseEntity<List<SolicitacaoAmizadeDTO>> enviadas(@PathVariable Long jogadorId) {
        List<SolicitacaoAmizadeDTO> enviadas = solicitacaoService.listarEnviadasPendentes(jogadorId)
                .stream().map(SolicitacaoAmizadeDTO::from).toList();
        return ResponseEntity.ok(enviadas);
    }

    /**
     * DELETE /api/solicitacoes/amizade/{jogadorId}/{amigoId}
     * Break an existing friendship (bidirectional). 204 on success.
     */
    @DeleteMapping("/amizade/{jogadorId}/{amigoId}")
    public ResponseEntity<Void> removerAmigo(@PathVariable Long jogadorId,
                                             @PathVariable Long amigoId) {
        solicitacaoService.removerAmigo(jogadorId, amigoId);
        return ResponseEntity.noContent().build();
    }
}
