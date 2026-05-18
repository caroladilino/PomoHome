package io.github.PomoHome.screens;

import com.badlogic.gdx.Screen;

/**
 * House screen. Renders the player's Casa with all its Slots, and lets the
 * player drag móveis from their inventory onto compatible slots.
 *
 * TODO (TEAM):
 *   show()
 *     -> apiClient.fetchCasaDoJogador(jogo.getJogadorLogado().getId(),
 *                                     cb that stores the Casa locally).
 *   render()
 *     -> Draw a background, then iterate slots and draw the móvel currently
 *        on each (or an "empty" indicator). Use Slot.nomePosicao to look up
 *        x/y from a positions config file.
 *   Drag-and-drop:
 *     - When the player drops a Movel onto a Slot:
 *         if (movel.getCategoria().equals(slot.getCategoriaPermitida())) {
 *             apiClient.colocarMovelNoSlot(slot.getId(), movel.getId(), cb);
 *         } else {
 *             // show "categoria incompatível" toast
 *         }
 *     - The backend will also reject incompatible pairs, but blocking it
 *       client-side gives instant feedback.
 *   "Like" button (when visiting a FRIEND's house):
 *     apiClient.darLike(casaVisitada.getId(), cb that updates numLikes).
 */
public class TelaCasa implements Screen {

    // TODO: fields — Jogo jogo, ApiClient apiClient, Casa casaSendoMostrada
    //       (the latter may be the logged-in player's OR a friend's — keep
    //        a boolean "souVisitante" to gate the like button).

    @Override public void show()                        { /* TODO */ }
    @Override public void render(float delta)           { /* TODO */ }
    @Override public void resize(int width, int height) { /* TODO */ }
    @Override public void pause()                       { }
    @Override public void resume()                      { }
    @Override public void hide()                        { }
    @Override public void dispose()                     { /* TODO: dispose Stage + Textures */ }
}
