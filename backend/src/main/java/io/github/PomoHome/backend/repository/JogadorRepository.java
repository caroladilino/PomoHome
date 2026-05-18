package io.github.PomoHome.backend.repository;

import io.github.PomoHome.backend.entity.Jogador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Data access layer for Jogador.
 *
 * Spring Data JPA gives us — for FREE, by extending JpaRepository — a full
 * CRUD API: save(), findById(), findAll(), deleteById(), count(), etc.
 *
 * On top of that we declare "derived query" methods. Spring parses the
 * METHOD NAME and writes the SQL for us:
 *   findByUsername(String x)                 -> WHERE username = ?
 *   findAllByOrderByTempoEstudadoDesc()      -> SELECT * ORDER BY tempo_estudado DESC
 *   findByCasa_Id(Long id)                   -> ... WHERE casa.id = ?
 */
@Repository
public interface JogadorRepository extends JpaRepository<Jogador, Long> {

    /** Used by login + uniqueness checks during sign-up. */
    Optional<Jogador> findByUsername(String username);

    /**
     * Powers GET /api/jogadores/ranking. Returns ALL players sorted by
     * tempoEstudado descending (highest study time first).
     */
    List<Jogador> findAllByOrderByTempoEstudadoDesc();

    /** True when the name is already taken — used during cadastrar(). */
    boolean existsByUsername(String username);

    /**
     * Every player who has this Movel in their inventory.
     * Used by MovelService.removerPorId to detach a móvel from all
     * inventories before deleting it (avoids a FK constraint violation
     * on the JOGADOR_INVENTARIO join table).
     */
    List<Jogador> findByInventario_Id(Long movelId);
}
