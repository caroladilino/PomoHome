package io.github.PomoHome.backend.service;

import io.github.PomoHome.backend.dto.DiaEstudoDTO;
import io.github.PomoHome.backend.dto.HistoricoSemanalDTO;
import io.github.PomoHome.backend.entity.Jogador;
import io.github.PomoHome.backend.entity.SessaoEstudo;
import io.github.PomoHome.backend.repository.JogadorRepository;
import io.github.PomoHome.backend.repository.SessaoEstudoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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

    /**
     * The player's study history for the current week (RF03): minutes studied
     * per day, Monday → Sunday, plus the weekly total. The week starts on the
     * most recent Monday (00:00). Days with no sessions report zero minutes, so
     * the result always has exactly 7 day entries.
     */
    @Transactional(readOnly = true)
    public HistoricoSemanalDTO historicoSemanal(Long jogadorId) {
        LocalDate inicioSemana = LocalDate.now().with(DayOfWeek.MONDAY);
        List<SessaoEstudo> sessoes = sessaoRepository
                .findByJogador_IdAndDataHoraGreaterThanEqualOrderByDataHoraAsc(
                        jogadorId, inicioSemana.atStartOfDay());

        int[] minutosPorDia = new int[7];
        for (SessaoEstudo s : sessoes) {
            long idx = ChronoUnit.DAYS.between(inicioSemana, s.getDataHora().toLocalDate());
            if (idx >= 0 && idx < 7) {
                minutosPorDia[(int) idx] += s.getMinutosConcluidos();
            }
        }

        List<DiaEstudoDTO> dias = new ArrayList<>(7);
        int total = 0;
        for (int i = 0; i < 7; i++) {
            dias.add(new DiaEstudoDTO(inicioSemana.plusDays(i), minutosPorDia[i]));
            total += minutosPorDia[i];
        }
        return new HistoricoSemanalDTO(inicioSemana, total, dias);
    }
}
