package io.github.PomoHome.screens;

import com.badlogic.gdx.Screen;

/**
 * Store screen. Shows the catalog as a grid and lets the player spend coins.
 *
 * TODO (TEAM):
 *   show()    -> jogo.getLoja().atualizarDoServidor(apiClient);
 *                build the grid actor (scene2d Table?).
 *   render()  -> ScreenUtils.clear(...); stage.act(delta); stage.draw();
 *                Re-build the grid only when the list changes; do NOT
 *                rebuild every frame.
 *   On "Comprar" click:
 *     apiClient.comprarMovel(jogo.getJogadorLogado().getId(), movel.getId(),
 *         new Callback<Jogador>() {
 *             public void onSuccess(Jogador j) {
 *                 Gdx.app.postRunnable(() -> jogo.setJogadorLogado(j));
 *             }
 *             public void onError(Throwable t) { ... toast ... }
 *         });
 *   resize/pause/resume/hide/dispose -> dispose the Stage in dispose().
 */
public class TelaLoja implements Screen {

    // TODO: hold references to `Jogo jogo` and `ApiClient apiClient` (constructor-injected).

    @Override public void show() {
        // TODO: build the UI Stage, set the input processor, request the catalog.
    }

    @Override public void render(float delta) {
        // TODO: clear + stage.act + stage.draw.
    }

    @Override public void resize(int width, int height) {
        // TODO: stage.getViewport().update(width, height, true);
    }

    @Override public void pause() { }
    @Override public void resume() { }
    @Override public void hide() { }

    @Override public void dispose() {
        // TODO: dispose the Stage and any Textures owned by this screen.
    }
}
