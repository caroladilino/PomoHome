package io.github.PomoHome;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import io.github.PomoHome.model.Casa;
import io.github.PomoHome.model.Jogador;
import io.github.PomoHome.model.Jogo;
import io.github.PomoHome.model.Movel;
import io.github.PomoHome.network.ApiClient;
import io.github.PomoHome.screens.TelaJogo;
import io.github.PomoHome.screens.TelaLogin;
import io.github.PomoHome.ui.UiSkin;

/**
 * Game entry point shared by all platforms.
 *
 * Owns the single instances every screen needs: the {@link ApiClient}, the
 * in-memory {@link Jogo} session, and the shared {@link Skin}. Screens get
 * here through their constructor (they take a {@code Main} and read these via
 * getters).
 *
 * Startup always shows {@link TelaLogin}. After login, {@link #entrarNoJogo}
 * builds the main {@link TelaJogo} <b>once</b> and caches it; the Ranking and
 * Friends screens return to that same instance via {@link #voltarAoJogo}, so
 * the in-progress timer / house grid survive a round trip.
 */
public class Main extends Game {

    private ApiClient apiClient;
    private Jogo jogo;
    private Skin skin;
    private TelaJogo telaJogo;

    @Override
    public void create() {
        apiClient = new ApiClient();
        jogo = new Jogo();
        skin = UiSkin.create();
        setScreen(new TelaLogin(this));
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public Jogo getJogo() {
        return jogo;
    }

    public Skin getSkin() {
        return skin;
    }

    /**
     * Called by {@link TelaLogin} once the server returns the authenticated
     * player. Prepares the player graph for the client (derives furniture tile
     * sizes, rebuilds the house placement map from the server slots), stores it
     * in the session, builds the cached game screen and switches to it.
     */
    public void entrarNoJogo(Jogador jogador) {
        prepararJogador(jogador);
        jogo.setJogadorLogado(jogador);
        telaJogo = new TelaJogo(this);
        setScreen(telaJogo);
    }

    /** Return to the main game screen (used by Ranking / Friends). */
    public void voltarAoJogo() {
        if (telaJogo != null) {
            setScreen(telaJogo);
        }
    }

    /** Derive client-only furniture sizes and rebuild the house grid from slots. */
    private void prepararJogador(Jogador jogador) {
        if (jogador == null) {
            return;
        }
        if (jogador.getInventario() != null) {
            for (Movel m : jogador.getInventario()) {
                m.resolverTamanho();
            }
        }
        Casa casa = jogador.getCasa();
        if (casa != null) {
            casa.fromSlots();
        }
    }

    @Override
    public void dispose() {
        super.dispose();      // disposes the current screen.
        if (telaJogo != null) {
            telaJogo.dispose(); // idempotent — safe even if it was the current screen.
        }
        if (skin != null) {
            skin.dispose();
        }
    }
}