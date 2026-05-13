package io.github.PomoHome.screens;

import com.badlogic.gdx.Screen;

/**
 * Ranking screen. Shows all players sorted by tempoEstudado DESC.
 *
 * TODO (TEAM):
 *   show()    -> jogo.getRanking().atualizarDoServidor(apiClient);
 *                build a Table with one row per player: position, username, tempoEstudado.
 *                Optionally highlight the logged-in player's row.
 *   render()  -> clear + stage.act + stage.draw.
 *   Re-build the Table rows whenever Ranking.rankingJogadores changes
 *   (Gdx.app.postRunnable inside the ApiClient callback is the right place
 *   to call a "refreshTable()" method here).
 */
public class TelaRanking implements Screen {

    // TODO: fields — Jogo jogo, ApiClient apiClient.

    @Override public void show()                        { /* TODO */ }
    @Override public void render(float delta)           { /* TODO */ }
    @Override public void resize(int width, int height) { /* TODO */ }
    @Override public void pause()                       { }
    @Override public void resume()                      { }
    @Override public void hide()                        { }
    @Override public void dispose()                     { /* TODO: dispose Stage */ }
}
