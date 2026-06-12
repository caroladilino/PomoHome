package io.github.PomoHome.backend.repository;

import io.github.PomoHome.backend.entity.SolicitacaoAmizade;
import io.github.PomoHome.backend.entity.StatusSolicitacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitacaoAmizadeRepository extends JpaRepository<SolicitacaoAmizade, Long> {

    /** Pending requests this player has RECEIVED (the "inbox"). */
    List<SolicitacaoAmizade> findByDestinatario_IdAndStatus(Long destinatarioId, StatusSolicitacao status);

    /** Pending requests this player has SENT (the "outbox"). */
    List<SolicitacaoAmizade> findByRemetente_IdAndStatus(Long remetenteId, StatusSolicitacao status);

    /**
     * A specific directed request between two players in a given status.
     * Used to detect (a) a duplicate request and (b) a reverse request that
     * lets us auto-accept instead of creating a second pending row.
     */
    Optional<SolicitacaoAmizade> findByRemetente_IdAndDestinatario_IdAndStatus(
            Long remetenteId, Long destinatarioId, StatusSolicitacao status);
}
