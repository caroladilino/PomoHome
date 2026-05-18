package io.github.PomoHome.backend.service;

import io.github.PomoHome.backend.entity.Movel;
import io.github.PomoHome.backend.repository.MovelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    public MovelService(MovelRepository movelRepository) {
        this.movelRepository = movelRepository;
    }

    /**
     * Full store catalog. Backs GET /api/loja.
     *
     * TODO: return movelRepository.findAll();
     */
    @Transactional(readOnly = true)
    public List<Movel> listarTodos() {
        // TODO: implement.
        return List.of();
    }

    @Transactional(readOnly = true)
    public Optional<Movel> buscarPorId(Long id) {
        // TODO: return movelRepository.findById(id);
        return Optional.empty();
    }

    @Transactional(readOnly = true)
    public List<Movel> listarPorCategoria(String categoria) {
        // TODO: return movelRepository.findByCategoria(categoria);
        return List.of();
    }

    /**
     * Admin-only — add a new product to the store.
     *
     * TODO (steps):
     *   1. Validate (preco > 0, nome/categoria non-blank).
     *   2. movelRepository.save(movel) and return the persisted entity.
     *
     * For the academic project you likely won't have real admin auth —
     * either gate it later with Spring Security or just leave the endpoint
     * "open" for now.
     */
    @Transactional
    public Movel criar(Movel movel) {
        // TODO: implement.
        return null;
    }

    @Transactional
    public void removerPorId(Long id) {
        // TODO: movelRepository.deleteById(id);
        //       Watch out: if any Slot still references this móvel as
        //       movelAtual, deleting it will throw a constraint violation.
        //       Either null those references first or change the relation
        //       to use ON DELETE SET NULL.
    }
}
