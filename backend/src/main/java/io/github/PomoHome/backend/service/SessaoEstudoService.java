package io.github.PomoHome.backend.service;

import io.github.PomoHome.backend.entity.Jogador;
import io.github.PomoHome.backend.entity.SessaoEstudo;
import io.github.PomoHome.backend.repository.JogadorRepository;
import io.github.PomoHome.backend.repository.SessaoEstudoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for Pomodoro sessions.
 *
 * The frontend (LibGDX) runs the local timer; when a session COMPLETES it
 * POSTs to /api/sessoes with the number of minutes. This service records
 * the session AND triggers the side effects (give coins, bump study time).
 */
@Service
public class SessaoEstudoService {

    private final SessaoEstudoRepository sessaoRepository;
    private final JogadorRepository jogadorRepository;
    private final JogadorService jogadorService;

    public SessaoEstudoService(SessaoEstudoRepository sessaoRepository,
                               JogadorRepository jogadorRepository,
                               JogadorService jogadorService) {
        this.sessaoRepository = sessaoRepository;
        this.jogadorRepository = jogadorRepository;
        this.jogadorService = jogadorService;
    }

    /**
     * Register a completed Pomodoro and reward the player.
     *
     * TODO (steps):
     *   1. Validate: minutosConcluidos > 0 (refuse negative/zero submissions).
     *   2. Load the Jogador (throw if not found).
     *   3. Build new SessaoEstudo(jogador, minutosConcluidos) — the
     *      constructor sets dataHora = LocalDateTime.now().
     *   4. sessaoRepository.save(sessao).
     *   5. Call jogadorService.creditarMoedas(jogadorId, minutosConcluidos)
     *      — that method bumps both saldo and tempoEstudado in one place.
     *   6. Return the saved SessaoEstudo.
     *
     *   Because this is @Transactional, both saves either commit together
     *   or rollback together — no inconsistent state where the session is
     *   recorded but the coins were never granted.
     */
    @Transactional
    public SessaoEstudo registrarSessao(Long jogadorId, int minutosConcluidos) {
        // TODO: implement following the steps above.
        return null;
    }

    @Transactional(readOnly = true)
    public List<SessaoEstudo> historicoDoJogador(Long jogadorId) {
        // TODO: return sessaoRepository.findByJogador_IdOrderByDataHoraDesc(jogadorId);
        return List.of();
    }
}
