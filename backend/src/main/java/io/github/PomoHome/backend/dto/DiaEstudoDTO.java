package io.github.PomoHome.backend.dto;

import java.time.LocalDate;

/**
 * One day's study total within a weekly history (RF03): the calendar date and
 * the sum of minutes studied that day. Part of {@link HistoricoSemanalDTO}.
 */
public record DiaEstudoDTO(LocalDate data, int minutos) {
}
