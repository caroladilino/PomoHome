package io.github.PomoHome.network;

import io.github.PomoHome.model.Casa;
import io.github.PomoHome.model.Jogador;
import io.github.PomoHome.model.Movel;
import io.github.PomoHome.model.SessaoEstudo;

import java.util.List;

/**
 * Thin REST client between the LibGDX game and the Spring Boot server.
 *
 * Why a dedicated class instead of calling Gdx.net everywhere?
 *   - centralizes the BASE_URL
 *   - centralizes JSON serialization (Gson)
 *   - lets us mock it in tests
 *
 * Threading reminder (CRITICAL — easy to miss):
 *   Gdx.net.sendHttpRequest is ASYNCHRONOUS. The callback runs on a
 *   background thread, NOT on the GL/render thread. Any UI mutation
 *   (touching scene2d actors, replacing Loja.itensDisponiveis if a
 *   render is in progress, etc.) MUST be wrapped in:
 *
 *       Gdx.app.postRunnable(() -> { ...UI update... });
 *
 * TODO (TEAM): the methods below are stubs. Read the implementation hint
 * inside fetchCatalogoLoja() — every other GET works the same way; every
 * POST adds a body via httpRequest.setContent(jsonString).
 */
public class ApiClient {

    /** All endpoints share this prefix. Change here if the server moves. */
    public static final String BASE_URL = "http://localhost:8080/api";

    // TODO: hold a Gson instance as a private final field:
    //   private final Gson gson = new Gson();
    // Avoid creating a new one per request — it's stateless and reusable.

    // ---------------------------------------------------------------
    // Callback type
    // ---------------------------------------------------------------

    /**
     * Generic two-method callback. We don't use libgdx's HttpResponseListener
     * directly so screens never have to think about HttpStatus codes.
     */
    public interface Callback<T> {
        void onSuccess(T result);
        void onError(Throwable t);
    }

    // ---------------------------------------------------------------
    // GETs
    // ---------------------------------------------------------------

    /**
     * GET /api/loja  -> List<Movel>
     *
     * TODO (this is the WORKED EXAMPLE — every other method follows the same shape):
     *
     *   HttpRequest req = new HttpRequest(HttpMethods.GET);
     *   req.setUrl(BASE_URL + "/loja");
     *   req.setHeader("Accept", "application/json");
     *
     *   Gdx.net.sendHttpRequest(req, new HttpResponseListener() {
     *       public void handleHttpResponse(HttpResponse r) {
     *           try {
     *               String json = r.getResultAsString();
     *               // For List<T> Gson needs a TypeToken to keep the generic:
     *               Type listType = new TypeToken<List<Movel>>(){}.getType();
     *               List<Movel> result = gson.fromJson(json, listType);
     *               cb.onSuccess(result);
     *           } catch (Exception e) {
     *               cb.onError(e);
     *           }
     *       }
     *       public void failed(Throwable t)   { cb.onError(t); }
     *       public void cancelled()           { cb.onError(new RuntimeException("cancelled")); }
     *   });
     */
    public void fetchCatalogoLoja(Callback<List<Movel>> cb) {
        // TODO: implement following the worked example above.
    }

    /** GET /api/jogadores/ranking -> List<Jogador> (already sorted DESC by tempoEstudado). */
    public void fetchRanking(Callback<List<Jogador>> cb) {
        // TODO: same shape as fetchCatalogoLoja, URL = BASE_URL + "/jogadores/ranking",
        //       TypeToken<List<Jogador>>(){}.getType()
    }

    /** GET /api/jogadores/username/{username} -> Jogador (used by friend search). */
    public void fetchJogadorPorUsername(String username, Callback<Jogador> cb) {
        // TODO: URL = BASE_URL + "/jogadores/username/" + URLEncoder.encode(username, "UTF-8")
        //       Single object -> gson.fromJson(json, Jogador.class)
    }

    /** GET /api/casas/jogador/{jogadorId} -> Casa */
    public void fetchCasaDoJogador(long jogadorId, Callback<Casa> cb) {
        // TODO: URL = BASE_URL + "/casas/jogador/" + jogadorId
    }

    // ---------------------------------------------------------------
    // POSTs / PUTs / DELETEs (with body when applicable)
    // ---------------------------------------------------------------

    /**
     * POST /api/sessoes  body { "jogadorId": ..., "minutosConcluidos": ... }
     * Returns the saved SessaoEstudo.
     *
     * TODO body building:
     *   String body = gson.toJson(Map.of(
     *       "jogadorId", jogadorId,
     *       "minutosConcluidos", minutos
     *   ));
     *   req.setMethod(HttpMethods.POST);
     *   req.setHeader("Content-Type", "application/json");
     *   req.setContent(body);
     */
    public void registrarSessao(long jogadorId, int minutos, Callback<SessaoEstudo> cb) {
        // TODO: implement.
    }

    /**
     * POST /api/loja/comprar  body { "jogadorId": ..., "movelId": ... }
     * Returns the updated Jogador (new saldo + new inventory).
     */
    public void comprarMovel(long jogadorId, long movelId, Callback<Jogador> cb) {
        // TODO: implement.
    }

    /** PUT /api/casas/slot/{slotId}/movel/{movelId} -> updated Casa */
    public void colocarMovelNoSlot(long slotId, long movelId, Callback<Casa> cb) {
        // TODO: PUT with no body; URL embeds both ids.
    }

    /** POST /api/casas/{id}/like -> updated Casa */
    public void darLike(long casaId, Callback<Casa> cb) {
        // TODO: POST with no body.
    }

    // ---------------------------------------------------------------
    // Account
    // ---------------------------------------------------------------

    /** POST /api/jogadores  body { "username": ..., "senha": ... } -> created Jogador */
    public void cadastrar(String username, String senha, Callback<Jogador> cb) {
        // TODO: implement.
    }

    /** POST /api/jogadores/login  body { "username": ..., "senha": ... } -> Jogador */
    public void login(String username, String senha, Callback<Jogador> cb) {
        // TODO: implement.
    }
}
