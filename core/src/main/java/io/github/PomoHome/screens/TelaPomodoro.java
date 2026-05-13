package io.github.PomoHome.screens;

import com.badlogic.gdx.Screen;

/**
 * Pomodoro screen. The core gameplay loop.
 *
 * TODO (TEAM):
 *   show()
 *     - Build the UI: big countdown digits, Start / Pause / Reset buttons.
 *   render(delta)
 *     - jogo.getTimer().tick(delta);  (or however the team's timer exposes its API)
 *     - if jogo.getTimer().isComplete() and we have NOT yet reported it:
 *         int minutos = jogo.getTimer().getMinutosCompletados();
 *         apiClient.registrarSessao(
 *             jogo.getJogadorLogado().getId(), minutos,
 *             new Callback<SessaoEstudo>() {
 *                 public void onSuccess(SessaoEstudo s) {
 *                     Gdx.app.postRunnable(() -> {
 *                         // give visual feedback: "+25 moedas"
 *                         // optionally GET /api/jogadores/{id} again to refresh saldo
 *                     });
 *                 }
 *                 public void onError(Throwable t) { ... retry queue ... }
 *             }
 *         );
 *
 *   IMPORTANT: send the POST exactly ONCE per completion. Keep a boolean
 *   "jaReportei" that resets when the user starts a new session.
 */
public class TelaPomodoro implements Screen {

    // TODO: fields — Jogo jogo, ApiClient apiClient, boolean jaReportei.

    @Override public void show()                        { /* TODO */ }
    @Override public void render(float delta)           { /* TODO */ }
    @Override public void resize(int width, int height) { /* TODO */ }
    @Override public void pause()                       { }
    @Override public void resume()                      { }
    @Override public void hide()                        { }
    @Override public void dispose()                     { /* TODO: dispose Stage + Textures */ }
}
