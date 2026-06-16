package io.github.PomoHome.backend.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * A player's study history for the current week (RF03). Holds the week's start
 * date (Monday), the per-day breakdown (Monday → Sunday, always 7 entries) and
 * the total minutes studied across the week.
 *
 * <p>Private by design: the frontend only ever requests the logged-in player's
 * own history, and the data carries no identity beyond the minutes themselves.
 */
public record HistoricoSemanalDTO(LocalDate inicioSemana,
                                  int totalMinutos,
                                  List<DiaEstudoDTO> dias) {
}
