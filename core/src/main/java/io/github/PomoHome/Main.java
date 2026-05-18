package io.github.PomoHome;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import io.github.PomoHome.model.Jogador;
import io.github.PomoHome.model.Jogo;
import io.github.PomoHome.network.ApiClient;
import io.github.PomoHome.screens.TelaLogin;
import io.github.PomoHome.screens.TelaPrincipal;
import io.github.PomoHome.ui.UiSkin;

/**
 * Game entry point shared by all platforms.
 *
 * Owns the single instances every screen needs: the {@link ApiClient}, the
 * in-memory {@link Jogo} session, and the shared {@link Skin}. Screens get
 * here through their constructor (pattern (a) from Jogo's docs) — they take
 * a {@code Main} and read these via getters.
 *
 * Startup always shows {@link TelaLogin}. Persistent / auto login will be
 * added later; for now every launch asks the user to sign in.
 */
public class Main extends Game {

    private ApiClient apiClient;
    private Jogo jogo;
    private Skin skin;

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
     * player (either after login or right after account creation). Stores
     * the player in the session and moves on to the main game screen.
     */
    public void entrarNoJogo(Jogador jogador) {
        jogo.setJogadorLogado(jogador);
        setScreen(new TelaPrincipal(this));
    }

    @Override
    public void dispose() {
        super.dispose();      // disposes the current screen.
        if (skin != null) {
            skin.dispose();
        }
    }
}
