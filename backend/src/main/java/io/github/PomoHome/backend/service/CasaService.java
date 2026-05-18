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
     * Increment the Casa's like counter (when a friend visits and likes
     * the house). No per-visitor dedup — a like can be given repeatedly.
     */
    @Transactional
    public Casa darLike(Long casaId) {
        Casa casa = casaRepository.findById(casaId)
                .orElseThrow(() -> new NoSuchElementException("Casa não encontrada"));
        casa.setNumLikes(casa.getNumLikes() + 1);
        return casaRepository.save(casa);
    }
}
