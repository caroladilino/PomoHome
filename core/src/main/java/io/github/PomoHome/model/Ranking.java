package io.github.PomoHome.model;

import io.github.PomoHome.network.ApiClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Client-side cached ranking. The list is already sorted DESC by
 * tempoEstudado when it arrives from the server (that's the contract of
 * GET /api/jogadores/ranking).
 */
public class Ranking {

    private List<Jogador> rankingJogadores = new ArrayList<>();

    public Ranking() { }

    /**
     * TODO (TEAM):
     *   1. api.fetchRanking(new ApiClient.Callback<List<Jogador>>() {
     *        public void onSuccess(List<Jogador> result) {
     *            rankingJogadores = result;
     *        }
     *        public void onError(Throwable t) { ... }
     *      });
     *   2. Call this once on TelaRanking.show() and optionally every N seconds.
     */
    public void atualizarDoServidor(ApiClient api) {
        // TODO: implement following the steps above.
    }

    public List<Jogador> getRankingJogadores() { return rankingJogadores; }
    public void setRankingJogadores(List<Jogador> rankingJogadores) { this.rankingJogadores = rankingJogadores; }
}
