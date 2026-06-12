package io.github.PomoHome.backend.service;

import io.github.PomoHome.backend.entity.Casa;
import io.github.PomoHome.backend.entity.Jogador;
import io.github.PomoHome.backend.entity.Movel;
import io.github.PomoHome.backend.entity.Slot;
import io.github.PomoHome.backend.repository.CasaRepository;
import io.github.PomoHome.backend.repository.JogadorRepository;
import io.github.PomoHome.backend.repository.MovelRepository;
import io.github.PomoHome.backend.repository.SlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Business logic for the player's house (Casa) and the Slot operations.
 *
 * This is where the "place a sofa only on a sofa slot" rule is enforced.
 */
@Service
public class CasaService {

    private final CasaRepository casaRepository;
    private final SlotRepository slotRepository;
    private final MovelRepository movelRepository;
    private final JogadorRepository jogadorRepository;

    public CasaService(CasaRepository casaRepository,
                       SlotRepository slotRepository,
                       MovelRepository movelRepository,
                       JogadorRepository jogadorRepository) {
        this.casaRepository = casaRepository;
        this.slotRepository = slotRepository;
        this.movelRepository = movelRepository;
        this.jogadorRepository = jogadorRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Casa> buscarPorJogador(Long jogadorId) {
        return casaRepository.findByDono_Id(jogadorId);
    }

    /**
     * Place a Movel from the player's inventory into a Slot. Enforces the
     * core rules: the player must own the móvel, and its categoria must
     * match the slot's categoriaPermitida. Returns the updated Casa.
     */
    @Transactional
    public Casa colocarMovelNoSlot(Long slotId, Long movelId) {
        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new NoSuchElementException("Slot não encontrado"));
        Movel movel = movelRepository.findById(movelId)
                .orElseThrow(() -> new NoSuchElementException("Móvel não encontrado"));

        Casa casa = slot.getCasa();
        if (casa == null) {
            throw new IllegalStateException("Slot não pertence a nenhuma casa");
        }
        Jogador dono = casa.getDono();
        if (dono == null || !dono.getInventario().contains(movel)) {
            throw new IllegalArgumentException("Jogador não possui esse móvel no inventário");
        }

        if (!slot.getCategoriaPermitida().equals(movel.getCategoria())) {
            throw new IllegalArgumentException(
                    "Móvel categoria '" + movel.getCategoria()
                            + "' não cabe em slot '" + slot.getCategoriaPermitida() + "'");
        }

        slot.setMovelAtual(movel);
        slotRepository.save(slot);
        return casa;
    }

    /**
     * Remove whatever Movel is currently in the Slot (leave it empty).
     * The móvel stays in the player's inventory — it just goes back to
     * "in storage".
     */
    @Transactional
    public Casa removerMovelDoSlot(Long slotId) {
        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new NoSuchElementException("Slot não encontrado"));
        slot.setMovelAtual(null);
        slotRepository.save(slot);
        return slot.getCasa();
    }

    /**
     * Toggle a visitor's like on the house: if {@code visitanteId} hasn't liked
     * it yet the like is added, otherwise it is removed. {@code numLikes} is
     * kept equal to the number of distinct likers, so a visitor can never stack
     * likes — at most one each, which they can also take back.
     */
    @Transactional
    public Casa darLike(Long casaId, Long visitanteId) {
        Casa casa = casaRepository.findById(casaId)
                .orElseThrow(() -> new NoSuchElementException("Casa não encontrada"));
        if (visitanteId == null) {
            throw new IllegalArgumentException("visitanteId é obrigatório");
        }
        if (!casa.getCurtidoPor().add(visitanteId)) {
            casa.getCurtidoPor().remove(visitanteId); // already liked -> unlike
        }
        casa.setNumLikes(casa.getCurtidoPor().size());
        return casaRepository.save(casa);
    }

    /**
     * Replace the whole house layout in one shot. Backs the free 8×8 grid:
     * the frontend, when it leaves "edit mode", sends every placed móvel as a
     * {@code (tileName, movelId)} pair. We wipe the Casa's current slots and
     * recreate one {@link Slot} per placement — {@code nomePosicao} = grid
     * tile name (e.g. "L3C5"), {@code movelAtual} = the placed móvel.
     *
     * <p>This is a free grid, so the per-category "sofa only on sofa slot"
     * rule does NOT apply here (that's {@link #colocarMovelNoSlot}). We still
     * keep {@code categoriaPermitida} populated (from the móvel) for info.
     *
     * <p>{@code orphanRemoval = true} on {@code Casa.slots} means clearing the
     * list deletes the old SLOT rows; adding new ones inserts them — all in
     * this single transaction.
     */
    @Transactional
    public Casa salvarLayout(Long casaId, String nome, List<Placement> placements) {
        Casa casa = casaRepository.findById(casaId)
                .orElseThrow(() -> new NoSuchElementException("Casa não encontrada"));

        // Persist the (optional) house name alongside the layout — both are
        // saved when the client leaves edit mode.
        if (nome != null && !nome.isBlank()) {
            casa.setNome(nome.trim());
        }

        casa.getSlots().clear();

        if (placements != null) {
            for (Placement p : placements) {
                if (p == null || p.tileName() == null || p.movelId() == null) {
                    throw new IllegalArgumentException("Placement inválido (tileName/movelId)");
                }
                Movel movel = movelRepository.findById(p.movelId())
                        .orElseThrow(() -> new NoSuchElementException(
                                "Móvel não encontrado: " + p.movelId()));
                Slot slot = new Slot(p.tileName(), movel.getCategoria());
                slot.setMovelAtual(movel);
                casa.addSlot(slot);
            }
        }

        return casaRepository.save(casa);
    }

    /** One placed móvel: which grid tile (anchor) holds which catalog móvel. */
    public record Placement(String tileName, Long movelId) { }

    /** Body of {@code PUT /api/casas/{id}/layout}: the house name + its placements. */
    public record LayoutRequest(String nome, List<Placement> placements) { }
}
