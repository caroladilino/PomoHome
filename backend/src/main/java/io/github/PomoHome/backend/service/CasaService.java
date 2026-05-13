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
        // TODO: return casaRepository.findByDono_Id(jogadorId);
        //       Used by "visit a friend's house" and "load my house".
        return Optional.empty();
    }

    /**
     * Place a Movel from the player's inventory into a Slot.
     *
     * TODO (steps — the core compatibility rule lives here):
     *   1. Load the Slot (throw if not found).
     *   2. Load the Movel (throw if not found).
     *   3. Load the Slot's owner Jogador via slot.getCasa().getDono().
     *      Verify the Movel is in dono.getInventario() — refuse to place
     *      furniture the player doesn't own.
     *   4. Validate compatibility:
     *        if (!slot.getCategoriaPermitida().equals(movel.getCategoria()))
     *            throw new IllegalArgumentException(
     *                "Móvel categoria '" + movel.getCategoria()
     *              + "' não cabe em slot '" + slot.getCategoriaPermitida() + "'");
     *   5. slot.setMovelAtual(movel);
     *   6. slotRepository.save(slot); (or rely on @Transactional dirty-checking)
     *   7. Return the updated Casa so the client can refresh its UI.
     */
    @Transactional
    public Casa colocarMovelNoSlot(Long slotId, Long movelId) {
        // TODO: implement following the steps above.
        return null;
    }

    /**
     * Remove whatever Movel is currently in the Slot (leave it empty).
     *
     * TODO (steps):
     *   1. Load the Slot.
     *   2. slot.setMovelAtual(null);
     *   3. Save and return the updated Casa.
     *   (We do NOT remove the móvel from the player's inventory — they
     *    still own it; it just goes back to "in storage".)
     */
    @Transactional
    public Casa removerMovelDoSlot(Long slotId) {
        // TODO: implement following the steps above.
        return null;
    }

    /**
     * Increment the Casa's like counter (when a friend visits and likes the house).
     *
     * TODO (steps):
     *   1. Load the Casa.
     *   2. casa.setNumLikes(casa.getNumLikes() + 1);
     *   3. save() and return.
     *
     * Optional but recommended: track who liked what, to prevent spam
     * (would need a LIKE_RELATION table — out of scope for the 1-month MVP).
     */
    @Transactional
    public Casa darLike(Long casaId) {
        // TODO: implement following the steps above.
        return null;
    }
}
