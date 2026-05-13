package io.github.PomoHome.backend.repository;

import io.github.PomoHome.backend.entity.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * No extra queries needed yet — JpaRepository's defaults
 * (save, findById, deleteById) are enough for the slot operations
 * we have planned (place / remove furniture).
 *
 * TODO (TEAM): when you implement "GET all slots of a given Casa", add
 *   List&lt;Slot&gt; findByCasa_Id(Long casaId);
 */
@Repository
public interface SlotRepository extends JpaRepository<Slot, Long> {
}
