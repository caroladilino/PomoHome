package io.github.PomoHome.backend.repository;

import io.github.PomoHome.backend.entity.SessaoEstudo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SessaoEstudoRepository extends JpaRepository<SessaoEstudo, Long> {

    /**
     * Per-player study history, most recent first.
     * Used by GET /api/sessoes/jogador/{id}.
     */
    List<SessaoEstudo> findByJogador_IdOrderByDataHoraDesc(Long jogadorId);

    /**
     * A player's sessions from {@code inicio} onward (oldest first).
     * Used by the weekly-history aggregation (RF03).
     */
    List<SessaoEstudo> findByJogador_IdAndDataHoraGreaterThanEqualOrderByDataHoraAsc(
            Long jogadorId, LocalDateTime inicio);
}
