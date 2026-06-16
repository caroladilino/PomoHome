package io.github.PomoHome.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Client mirror of the backend {@code HistoricoSemanalDTO}: the logged-in
 * player's study time for the current week (RF03) — the week's start date, the
 * per-day breakdown (Monday → Sunday, 7 entries) and the weekly total in minutes.
 * Field names must match the DTO for Gson.
 */
public class HistoricoSemanal {
    private String inicioSemana;          // ISO-8601 date (the week's Monday)
    private int totalMinutos;
    private List<DiaEstudo> dias = new ArrayList<>();

    public HistoricoSemanal() { }

    public String getInicioSemana() { return inicioSemana; }
    public void setInicioSemana(String inicioSemana) { this.inicioSemana = inicioSemana; }

    public int getTotalMinutos() { return totalMinutos; }
    public void setTotalMinutos(int totalMinutos) { this.totalMinutos = totalMinutos; }

    public List<DiaEstudo> getDias() { return dias; }
    public void setDias(List<DiaEstudo> dias) { this.dias = dias; }
}
