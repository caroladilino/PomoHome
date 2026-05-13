package io.github.PomoHome.model;

import io.github.PomoHome.network.ApiClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Client-side view of the store. Holds the cached catalog and knows how to
 * refresh it from the server.
 *
 * Why a class and not just a List? Because TelaLoja and ApiClient can both
 * hold a reference to the SAME Loja instance — when ApiClient updates it,
 * TelaLoja just re-renders from the same list. (Simpler than passing the
 * list around.)
 */
public class Loja {

    private List<Movel> itensDisponiveis = new ArrayList<>();

    public Loja() { }

    /**
     * Asks the server for the current catalog and replaces 'itensDisponiveis'
     * when the response arrives.
     *
     * TODO (TEAM):
     *   1. api.fetchCatalogoLoja(new ApiClient.Callback<List<Movel>>() {
     *        public void onSuccess(List<Movel> result) {
     *            itensDisponiveis = result;
     *            // If your UI reads this from the LibGDX render thread,
     *            // wrap the assignment in Gdx.app.postRunnable(() -> ...).
     *        }
     *        public void onError(Throwable t) { ... show a toast ... }
     *      });
     */
    public void atualizarDoServidor(ApiClient api) {
        // TODO: implement following the steps above.
    }

    public List<Movel> getItensDisponiveis() { return itensDisponiveis; }
    public void setItensDisponiveis(List<Movel> itensDisponiveis) { this.itensDisponiveis = itensDisponiveis; }
}
