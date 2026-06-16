package io.github.PomoHome.model;

/**
 * Client mirror of the backend {@code DiaEstudoDTO}: one day's study total in a
 * weekly history (RF03). {@code data} is an ISO-8601 date string from the server
 * (e.g. "2026-06-15"); field names must match the DTO for Gson.
 */
public class DiaEstudo {
    private String data;     // ISO-8601 date from the server
    private int minutos;

    public DiaEstudo() { }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public int getMinutos() { return minutos; }
    public void setMinutos(int minutos) { this.minutos = minutos; }
}
