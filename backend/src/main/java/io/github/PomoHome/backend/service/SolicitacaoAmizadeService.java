package io.github.PomoHome.backend.service;

import io.github.PomoHome.backend.entity.Jogador;
import io.github.PomoHome.backend.entity.SolicitacaoAmizade;
import io.github.PomoHome.backend.entity.StatusSolicitacao;
import io.github.PomoHome.backend.repository.JogadorRepository;
import io.github.PomoHome.backend.repository.SolicitacaoAmizadeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Friend-request handshake.
 *
 * The canonical "who is friends with whom" still lives in
 * Jogador.amigosIds. This service only manages the PENDING -> ACCEPTED /
 * REJECTED transition and, on acceptance, writes the friendship into both
 * players' amigosIds (bidirectional).
 */
@Service
public class SolicitacaoAmizadeService {

    private final SolicitacaoAmizadeRepository solicitacaoRepository;
    private final JogadorRepository jogadorRepository;

    public SolicitacaoAmizadeService(SolicitacaoAmizadeRepository solicitacaoRepository,
                                     JogadorRepository jogadorRepository) {
        this.solicitacaoRepository = solicitacaoRepository;
        this.jogadorRepository = jogadorRepository;
    }

    /**
     * Send a friend request from 'remetenteId' to 'destinatarioId'.
     *
     * Special case requested by the team: if the destinatário ALREADY has a
     * pending request pointing back at the remetente (i.e. they both want to
     * be friends), we don't create a second row — we just accept the existing
     * one. Mutual interest = instant friendship.
     */
    @Transactional
    public SolicitacaoAmizade enviarSolicitacao(Long remetenteId, Long destinatarioId) {
        if (remetenteId.equals(destinatarioId)) {
            throw new IllegalArgumentException("Jogador não pode enviar solicitação para si mesmo");
        }

        Jogador remetente = jogadorRepository.findById(remetenteId)
                .orElseThrow(() -> new NoSuchElementException("Remetente não encontrado"));
        Jogador destinatario = jogadorRepository.findById(destinatarioId)
                .orElseThrow(() -> new NoSuchElementException("Destinatário não encontrado"));

        if (remetente.getAmigosIds().contains(destinatarioId)) {
            throw new IllegalArgumentException("Jogadores já são amigos");
        }

        // (1) Reverse pending request exists -> mutual interest, accept it now.
        Optional<SolicitacaoAmizade> reversa = solicitacaoRepository
                .findByRemetente_IdAndDestinatario_IdAndStatus(
                        destinatarioId, remetenteId, StatusSolicitacao.PENDENTE);
        if (reversa.isPresent()) {
            return aceitar(reversa.get().getId());
        }

        // (2) Forward pending request already exists -> idempotent, return it.
        Optional<SolicitacaoAmizade> existente = solicitacaoRepository
                .findByRemetente_IdAndDestinatario_IdAndStatus(
                        remetenteId, destinatarioId, StatusSolicitacao.PENDENTE);
        if (existente.isPresent()) {
            return existente.get();
        }

        // (3) Brand-new request.
        SolicitacaoAmizade nova = new SolicitacaoAmizade(remetente, destinatario);
        return solicitacaoRepository.save(nova);
    }

    /**
     * Accept a pending request. Makes the friendship bidirectional and marks
     * the request ACEITA.
     */
    @Transactional
    public SolicitacaoAmizade aceitar(Long solicitacaoId) {
        SolicitacaoAmizade s = solicitacaoRepository.findById(solicitacaoId)
                .orElseThrow(() -> new NoSuchElementException("Solicitação não encontrada"));
        if (s.getStatus() != StatusSolicitacao.PENDENTE) {
            throw new IllegalArgumentException("Solicitação não está pendente");
        }

        Jogador remetente = s.getRemetente();
        Jogador destinatario = s.getDestinatario();

        if (!remetente.getAmigosIds().contains(destinatario.getId())) {
            remetente.getAmigosIds().add(destinatario.getId());
        }
        if (!destinatario.getAmigosIds().contains(remetente.getId())) {
            destinatario.getAmigosIds().add(remetente.getId());
        }
        jogadorRepository.save(remetente);
        jogadorRepository.save(destinatario);

        s.setStatus(StatusSolicitacao.ACEITA);
        return solicitacaoRepository.save(s);
    }

    /** Reject a pending request. No friendship is created. */
    @Transactional
    public SolicitacaoAmizade recusar(Long solicitacaoId) {
        SolicitacaoAmizade s = solicitacaoRepository.findById(solicitacaoId)
                .orElseThrow(() -> new NoSuchElementException("Solicitação não encontrada"));
        if (s.getStatus() != StatusSolicitacao.PENDENTE) {
            throw new IllegalArgumentException("Solicitação não está pendente");
        }
        s.setStatus(StatusSolicitacao.RECUSADA);
        return solicitacaoRepository.save(s);
    }

    /** Pending requests this player RECEIVED (their inbox). */
    @Transactional(readOnly = true)
    public List<SolicitacaoAmizade> listarRecebidasPendentes(Long jogadorId) {
        return solicitacaoRepository.findByDestinatario_IdAndStatus(
                jogadorId, StatusSolicitacao.PENDENTE);
    }

    /** Pending requests this player SENT (their outbox). */
    @Transactional(readOnly = true)
    public List<SolicitacaoAmizade> listarEnviadasPendentes(Long jogadorId) {
        return solicitacaoRepository.findByRemetente_IdAndStatus(
                jogadorId, StatusSolicitacao.PENDENTE);
    }

    /**
     * Break an existing friendship. Removes each player from the other's
     * amigosIds (bidirectional). Idempotent on the list level, but we
     * reject the call outright if they aren't actually friends so the
     * client gets clear feedback.
     *
     * Note: the historical ACEITA SolicitacaoAmizade row is intentionally
     * left untouched (see StatusSolicitacao docs). A future friend request
     * between the two will simply create a fresh PENDENTE row.
     */
    @Transactional
    public void removerAmigo(Long jogadorId, Long amigoId) {
        if (jogadorId.equals(amigoId)) {
            throw new IllegalArgumentException("Jogador não pode remover a si mesmo");
        }

        Jogador jogador = jogadorRepository.findById(jogadorId)
                .orElseThrow(() -> new NoSuchElementException("Jogador não encontrado"));
        Jogador amigo = jogadorRepository.findById(amigoId)
                .orElseThrow(() -> new NoSuchElementException("Amigo não encontrado"));

        if (!jogador.getAmigosIds().contains(amigoId)) {
            throw new IllegalArgumentException("Jogadores não são amigos");
        }

        jogador.getAmigosIds().remove(amigoId);
        amigo.getAmigosIds().remove(jogadorId);
        jogadorRepository.save(jogador);
        jogadorRepository.save(amigo);
    }
}
