package io.github.PomoHome.backend.controller;

import io.github.PomoHome.backend.dto.JogadorDTO;
import io.github.PomoHome.backend.dto.MovelDTO;
import io.github.PomoHome.backend.entity.Movel;
import io.github.PomoHome.backend.service.JogadorService;
import io.github.PomoHome.backend.service.MovelService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST endpoints under /api/loja.
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
     * GET /api/loja            -> full catalog
     * GET /api/loja?categoria=sofa  -> only that category
     *
     * The optional 'categoria' query param lets TelaLoja filter the store
     * (e.g. show only sofás) without a separate endpoint.
     */
    @GetMapping
    public ResponseEntity<List<MovelDTO>> listarTodos(
            @RequestParam(required = false) String categoria) {
        var moveis = (categoria == null || categoria.isBlank())
                ? movelService.listarTodos()
                : movelService.listarPorCategoria(categoria);
        List<MovelDTO> catalogo = moveis.stream()
                .map(MovelDTO::from)
                .toList();
        return ResponseEntity.ok(catalogo);
    }

    /**
     * GET /api/loja/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<MovelDTO> buscarPorId(@PathVariable Long id) {
        return movelService.buscarPorId(id)
                .map(MovelDTO::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * POST /api/loja  (admin)
     * Body: a full Movel JSON.
     */
    @PostMapping
    public ResponseEntity<MovelDTO> criar(@RequestBody Movel body) {
        return ResponseEntity.status(201).body(MovelDTO.from(movelService.criar(body)));
    }

    /**
     * DELETE /api/loja/{id}  (admin)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        movelService.removerPorId(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/loja/comprar
     * Body: { "jogadorId": 1, "movelId": 7 }
     *
     * NB: we delegate to JogadorService here because the purchase mutates
     * Jogador (saldo + inventário), not Movel.
     */
    @PostMapping("/comprar")
    public ResponseEntity<JogadorDTO> comprar(@RequestBody Map<String, Object> body) {
        Long jogadorId = Long.valueOf(body.get("jogadorId").toString());
        Long movelId = Long.valueOf(body.get("movelId").toString());
        return ResponseEntity.ok(JogadorDTO.from(jogadorService.comprarMovel(jogadorId, movelId)));
    }
}
