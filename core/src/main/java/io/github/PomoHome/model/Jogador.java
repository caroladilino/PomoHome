package io.github.PomoHome.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Client-side mirror of the backend's Jogador entity.
 *
 * The JSON returned by GET /api/jogadores/{id} fills this object via Gson.
 *
 * TODO (TEAM):
 *   - 'senha' will be present in the JSON for now (until we add a DTO on
 *     the backend). DO NOT log this field, DO NOT display it on the UI.
 *   - 'amigosIds' is just a list of player ids. To display friends you'll
 *     need to call GET /api/jogadores/{id} for each — consider caching.
 */
public class Jogador {
    private Long id;
    private String username;
    private String senha;            // see TODO above
    private int saldo;
    private int tempoEstudado;
    private Casa casa;
    private List<Movel> inventario = new ArrayList<>();
    private List<Long> amigosIds = new ArrayList<>();

    public Jogador() { }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public int getSaldo() { return saldo; }
    public void setSaldo(int saldo) { this.saldo = saldo; }

    public int getTempoEstudado() { return tempoEstudado; }
    public void setTempoEstudado(int tempoEstudado) { this.tempoEstudado = tempoEstudado; }

    public Casa getCasa() { return casa; }
    public void setCasa(Casa casa) { this.casa = casa; }

    public List<Movel> getInventario() { return inventario; }
    public void setInventario(List<Movel> inventario) { this.inventario = inventario; }

    public List<Long> getAmigosIds() { return amigosIds; }
    public void setAmigosIds(List<Long> amigosIds) { this.amigosIds = amigosIds; }
}
