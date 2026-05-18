package io.github.PomoHome.backend.service;

import io.github.PomoHome.backend.entity.Jogador;
import io.github.PomoHome.backend.entity.SessaoEstudo;
import io.github.PomoHome.backend.repository.JogadorRepository;
import io.github.PomoHome.backend.repository.SessaoEstudoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

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
     * Register a completed Pomodoro and reward the player. Saving the
     * session and crediting coins share one @Transactional, so they commit
     * or roll back together — never a session without its coins.
     */
    @Transactional
    public SessaoEstudo registrarSessao(Long jogadorId, int minutosConcluidos) {
        if (minutosConcluidos <= 0) {
            throw new IllegalArgumentException("Minutos concluídos deve ser maior que zero");
        }
        Jogador jogador = jogadorRepository.findById(jogadorId)
                .orElseThrow(() -> new NoSuchElementException("Jogador não encontrado"));

        SessaoEstudo sessao = new SessaoEstudo(jogador, minutosConcluidos);
        SessaoEstudo salva = sessaoRepository.save(sessao);

        jogadorService.creditarMoedas(jogadorId, minutosConcluidos);
        return salva;
    }

    @Transactional(readOnly = true)
    public List<SessaoEstudo> historicoDoJogador(Long jogadorId) {
        return sessaoRepository.findByJogador_IdOrderByDataHoraDesc(jogadorId);
    }
}
