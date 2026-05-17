package io.github.PomoHome.backend.controller;

import io.github.PomoHome.backend.entity.Jogador;
import io.github.PomoHome.backend.service.JogadorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST endpoints under /api/jogadores.
 *
 * Layering reminder: a controller is THIN. It only:
 *   1. Maps HTTP -> service method call
 *   2. Maps the service's return -> HTTP response (status + body)
 *   3. Translates exceptions -> error responses (or lets a
 *      @ControllerAdvice do it globally)
 *
 * It MUST NOT contain business rules — those live in the @Service.
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
     *
     * TODO:
     *   1. Read username + senha from the body (use a CadastrarRequest DTO,
     *      or a Map<String,String> for the prototype).
     *   2. jogadorService.cadastrar(username, senha)
     *   3. Return ResponseEntity.status(HttpStatus.CREATED).body(novoJogador);
     */
    @PostMapping
    public ResponseEntity<Jogador> cadastrar(@RequestBody Map<String, String> body) {
        Jogador novo = jogadorService.cadastrar(body.get("username"), body.get("senha"));
        return ResponseEntity.status(201).body(novo);
    }

    /**
     * POST /api/jogadores/login
     * Body: { "username": "...", "senha": "..." }
     *
     * TODO: jogadorService.autenticar(...); on success return 200 + Jogador,
     *       on failure return 401 (handled either here or via @ControllerAdvice).
     */
    @PostMapping("/login")
    public ResponseEntity<Jogador> login(@RequestBody Map<String, String> body) {
        Jogador jogador = jogadorService.autenticar(body.get("username"), body.get("senha"));
        return ResponseEntity.ok(jogador);
    }

    /**
     * GET /api/jogadores/{id}
     * Used when a friend's profile / house is fetched.
     *
     * TODO: jogadorService.buscarPorId(id)
     *         .map(ResponseEntity::ok)
     *         .orElseGet(() -> ResponseEntity.notFound().build());
     */
    @GetMapping("/{id}")
    public ResponseEntity<Jogador> buscarPorId(@PathVariable Long id) {
        return jogadorService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * GET /api/jogadores/username/{username}
     * Backs the "find a friend by username" search bar on the frontend.
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<Jogador> buscarPorUsername(@PathVariable String username) {
        return jogadorService.buscarPorUsername(username)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * POST /api/jogadores/{id}/amigos/{amigoId}
     * Adds amigoId to jogadorId's friend list.
     *
     * TODO: jogadorService.adicionarAmigo(id, amigoId); return 200 + Jogador.
     */
    @PostMapping("/{id}/amigos/{amigoId}")
    public ResponseEntity<Jogador> adicionarAmigo(@PathVariable Long id, @PathVariable Long amigoId) {
        return ResponseEntity.ok(jogadorService.adicionarAmigo(id, amigoId));
    }

    /**
     * GET /api/jogadores/ranking
     * Returns all players sorted by tempoEstudado DESC.
     *
     * TODO: return ResponseEntity.ok(jogadorService.listarRanking());
     *
     * Design note: we put the ranking endpoint under /jogadores rather than
     * a separate /ranking root because it returns Jogador objects. Either
     * layout is fine — pick one and document it.
     */
    @GetMapping("/ranking")
    public ResponseEntity<List<Jogador>> ranking() {
        return ResponseEntity.ok(jogadorService.listarRanking());
    }
}
