package io.github.PomoHome.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * A single completed Pomodoro session.
 *
 * Each row records: who studied, when, and how many minutes were completed.
 * Aggregating these rows yields the per-player history (Historico) and
 * also feeds Jogador.tempoEstudado / Jogador.saldo (1 minute = 1 coin).
 *
 * TODO (TEAM):
 *   - Always create rows through SessaoEstudoService.registrarSessao() so
 *     the side effects (credit coins, bump tempoEstudado) stay consistent.
 *     Do NOT update Jogador directly from the controller.
 *   - Consider adding a 'duracaoSegundos' if you want fractional credits
 *     (e.g. a session aborted at 24:30 still counts as 24 minutes).
 */
@Entity
@Table(name = "SESSAO_ESTUDO")
public class SessaoEstudo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Who completed the session.
     * @JsonIgnore avoids serializing the whole Jogador (with its inventory,
     * casa, etc.) every time we fetch a session. The client already knows
     * which player asked for the history.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "jogador_id", nullable = false)
    @JsonIgnore
    private Jogador jogador;

    // TODO: defaulted at construction time in the service. Stored as TIMESTAMP.
    @Column(nullable = false)
    private LocalDateTime dataHora;

    @Column(nullable = false)
    private int minutosConcluidos;

    public SessaoEstudo() { }

    public SessaoEstudo(Jogador jogador, int minutosConcluidos) {
        this.jogador = jogador;
        this.minutosConcluidos = minutosConcluidos;
        this.dataHora = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Jogador getJogador() { return jogador; }
    public void setJogador(Jogador jogador) { this.jogador = jogador; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }

    public int getMinutosConcluidos() { return minutosConcluidos; }
    public void setMinutosConcluidos(int minutosConcluidos) { this.minutosConcluidos = minutosConcluidos; }
}
