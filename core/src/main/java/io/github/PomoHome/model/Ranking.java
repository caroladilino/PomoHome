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

    public void atualizarDoServidor(ApiClient api) {
        atualizarDoServidor(api, null, null);
    }

    /**
     * Fetch the ranking and store it. {@code aoAtualizar} runs on success and
     * {@code aoFalhar} on error — both on the network thread, so a screen that
     * mutates UI from them must wrap that work in {@code Gdx.app.postRunnable}.
     */
    public void atualizarDoServidor(ApiClient api, Runnable aoAtualizar, Runnable aoFalhar) {
        api.fetchRanking(new ApiClient.Callback<List<Jogador>>() {
            @Override
            public void onSuccess(List<Jogador> result) {
                if (result != null) {
                    rankingJogadores = result;
                }
                if (aoAtualizar != null) {
                    aoAtualizar.run();
                }
            }

            @Override
            public void onError(Throwable t) {
                if (aoFalhar != null) {
                    aoFalhar.run();
                }
            }
        });
    }

    public List<Jogador> getRankingJogadores() { return rankingJogadores; }
    public void setRankingJogadores(List<Jogador> rankingJogadores) { this.rankingJogadores = rankingJogadores; }
}
