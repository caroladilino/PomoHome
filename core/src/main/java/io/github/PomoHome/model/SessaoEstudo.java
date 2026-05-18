package io.github.PomoHome.model;

/**
 * Client-side mirror of the backend's SessaoEstudo entity.
 *
 * 'dataHora' is sent by the server as an ISO-8601 string (e.g.
 * "2026-05-13T14:30:00"). For the prototype we keep it as a String here
 * and let the UI format it.
 *
 * TODO (TEAM): if you want to do date arithmetic in the client, you can
 * register a Gson TypeAdapter for java.time.LocalDateTime in ApiClient.
 */
public class SessaoEstudo {
    private Long id;
    private String dataHora;       // ISO-8601 string from the server
    private int minutosConcluidos;

    public SessaoEstudo() { }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDataHora() { return dataHora; }
    public void setDataHora(String dataHora) { this.dataHora = dataHora; }

    public int getMinutosConcluidos() { return minutosConcluidos; }
    public void setMinutosConcluidos(int minutosConcluidos) { this.minutosConcluidos = minutosConcluidos; }
}
