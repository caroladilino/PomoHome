package io.github.PomoHome.backend.entity;

/**
 * Lifecycle of a friend request.
 *
 *   PENDENTE  -> waiting for the recipient to act
 *   ACEITA    -> recipient accepted; both players are now friends
 *   RECUSADA  -> recipient rejected; no friendship created
 *
 * We keep ACEITA/RECUSADA rows around (instead of deleting them) so the
 * team can later show a "histórico de solicitações" screen if desired.
 */
public enum StatusSolicitacao {
    PENDENTE,
    ACEITA,
    RECUSADA
}
