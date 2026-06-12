package io.github.PomoHome.backend.service;

import io.github.PomoHome.backend.entity.Jogador;
import io.github.PomoHome.backend.entity.Movel;
import io.github.PomoHome.backend.entity.Slot;
import io.github.PomoHome.backend.repository.JogadorRepository;
import io.github.PomoHome.backend.repository.MovelRepository;
import io.github.PomoHome.backend.repository.SlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Business logic for Movel and the "Loja" (store) view.
 *
 * Reminder: the design merged ItemLoja into Movel, so this service IS
 * the store service.
 */
@Service
public class MovelService {

    private final MovelRepository movelRepository;
    private final SlotRepository slotRepository;
    private final JogadorRepository jogadorRepository;

    public MovelService(MovelRepository movelRepository,
                        SlotRepository slotRepository,
                        JogadorRepository jogadorRepository) {
        this.movelRepository = movelRepository;
        this.slotRepository = slotRepository;
        this.jogadorRepository = jogadorRepository;
    }

    /** Full store catalog. Backs GET /api/loja. */
    @Transactional(readOnly = true)
    public List<Movel> listarTodos() {
        return movelRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Movel> buscarPorId(Long id) {
        return movelRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Movel> listarPorCategoria(String categoria) {
        return movelRepository.findByCategoria(categoria);
    }

    /**
     * Admin-only — add a new product to the store. Validates that nome and
     * categoria are non-blank and preco is positive. The endpoint is
     * currently unauthenticated (academic scope).
     */
    @Transactional
    public Movel criar(Movel movel) {
        if (movel.getNome() == null || movel.getNome().isBlank()) {
            throw new IllegalArgumentException("Nome não pode ser vazio");
        }
        if (movel.getCategoria() == null || movel.getCategoria().isBlank()) {
            throw new IllegalArgumentException("Categoria não pode ser vazia");
        }
        if (movel.getPreco() <= 0) {
            throw new IllegalArgumentException("Preço deve ser maior que zero");
        }
        return movelRepository.save(movel);
    }

    /**
     * Remove a móvel from the catalog.
     *
     * A Movel can be referenced from two places:
     *   - SLOT.movel_atual_id  (a house has it placed)
     *   - JOGADOR_INVENTARIO   (a player owns it)
     * Deleting the row directly would throw a FK constraint violation.
     * So we detach those references first, then delete.
     */
    @Transactional
    public void removerPorId(Long id) {
        Movel movel = movelRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Móvel não encontrado"));

        // 1. Clear it out of any slot that has it placed.
        for (Slot slot : slotRepository.findByMovelAtual_Id(id)) {
            slot.setMovelAtual(null);
            slotRepository.save(slot);
        }

        // 2. Remove it from every player's inventory.
        for (Jogador jogador : jogadorRepository.findByInventario_Id(id)) {
            jogador.getInventario().removeIf(m -> m.getId().equals(id));
            jogadorRepository.save(jogador);
        }

        // 3. Now safe to delete.
        movelRepository.delete(movel);
    }
}
