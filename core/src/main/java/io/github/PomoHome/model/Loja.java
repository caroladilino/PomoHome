package io.github.PomoHome.model;

import com.badlogic.gdx.Gdx;
import io.github.PomoHome.network.ApiClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Client-side view of the store: the cached catalog plus a refresh from the
 * server. Screens hold the same {@code Loja} instance and re-render from it
 * after each refresh.
 */
public class Loja {

    private List<Movel> itensDisponiveis = new ArrayList<>();

    public Loja() { }

    public void atualizarDoServidor(ApiClient api) {
        atualizarDoServidor(api, null);
    }

    /**
     * Same as {@link #atualizarDoServidor(ApiClient)}, but runs {@code aoAtualizar}
     * (on the render thread) once the catalog has been replaced — handy for
     * rebuilding a scene2d store table when the data arrives.
     */
    public void atualizarDoServidor(ApiClient api, Runnable aoAtualizar) {
        api.fetchCatalogoLoja(new ApiClient.Callback<List<Movel>>() {
            @Override
            public void onSuccess(List<Movel> result) {
                Gdx.app.postRunnable(() -> {
                    if (result != null) {
                        for (Movel m : result) {
                            m.resolverTamanho(); // derive tile footprint from categoria
                        }
                        itensDisponiveis = result;
                    }
                    if (aoAtualizar != null) {
                        aoAtualizar.run();
                    }
                });
            }

            @Override
            public void onError(Throwable t) {
                // Leave the previous catalog in place; the screen can surface
                // the error. status == 0 means the backend is unreachable.
            }
        });
    }

    public List<Movel> getItensDisponiveis() { return itensDisponiveis; }
    public void setItensDisponiveis(List<Movel> itensDisponiveis) { this.itensDisponiveis = itensDisponiveis; }
}
