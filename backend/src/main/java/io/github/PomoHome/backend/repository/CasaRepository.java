package io.github.PomoHome.backend.repository;

import io.github.PomoHome.backend.entity.Casa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CasaRepository extends JpaRepository<Casa, Long> {

    /**
     * Lookup the house of a given player.
     *
     * The "_" syntax tells Spring to traverse the relation:
     *   findByDono_Id  ==  WHERE casa.dono.id = ?
     *
     * Note: Casa.dono is the INVERSE side, so under the hood JPA joins
     * JOGADOR ON jogador.casa_id = casa.id WHERE jogador.id = ?.
     */
    Optional<Casa> findByDono_Id(Long jogadorId);
}
