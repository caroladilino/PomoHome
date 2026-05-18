package io.github.PomoHome.backend.repository;

import io.github.PomoHome.backend.entity.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Data access for Slot. JpaRepository covers the basic CRUD used by the
 * place / remove furniture flow; the derived query below supports móvel
 * deletion.
 */
@Repository
public interface SlotRepository extends JpaRepository<Slot, Long> {

    /**
     * Every slot currently holding this Movel. Used by
     * MovelService.removerPorId to clear movelAtual before deleting the
     * móvel (avoids a FK constraint violation on SLOT.movel_atual_id).
     */
    List<Slot> findByMovelAtual_Id(Long movelId);
}
