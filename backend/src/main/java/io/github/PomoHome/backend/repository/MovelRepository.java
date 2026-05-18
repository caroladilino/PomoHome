package io.github.PomoHome.backend.repository;

import io.github.PomoHome.backend.entity.Movel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Furniture catalog. The "Loja" (store) is just a view over this table:
 * GET /api/loja  ==  movelRepository.findAll().
 */
@Repository
public interface MovelRepository extends JpaRepository<Movel, Long> {

    /** Filter the store by category, e.g. only sofás. */
    List<Movel> findByCategoria(String categoria);
}
