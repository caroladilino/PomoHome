package io.github.PomoHome.network;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.HttpMethods;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.PomoHome.model.Casa;
import io.github.PomoHome.model.Jogador;
import io.github.PomoHome.model.Movel;
import io.github.PomoHome.model.SessaoEstudo;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Thin REST client between the LibGDX game and the Spring Boot server.
 *
 * Why a dedicated class instead of calling Gdx.net everywhere?
 *   - centralizes the BASE_URL
 *   - centralizes JSON serialization (Gson)
 *   - centralizes error handling (non-2xx -> {@link ApiException} with the
 *     server's plain-text message)
 *
 * Threading reminder (CRITICAL — easy to miss):
 *   Gdx.net.sendHttpRequest is ASYNCHRONOUS. The callback runs on a
 *   background thread, NOT on the GL/render thread. Any UI mutation done
 *   from {@link Callback#onSuccess}/{@link Callback#onError} MUST be wrapped
 *   in Gdx.app.postRunnable(...). The screens are responsible for that —
 *   this client only does network + JSON.
 */
public class ApiClient {

    /** All endpoints share this prefix. Change here if the server moves. */
    public static final String BASE_URL = "http://localhost:8080/api";

    /** Gson is stateless and thread-safe — one shared instance is fine. */
    private final Gson gson = new Gson();

    // ---------------------------------------------------------------
    // Callback type
    // ---------------------------------------------------------------

    /**
     * Generic two-method callback. We don't expose libgdx's
     * HttpResponseListener so screens never have to think about HTTP codes.
     * On failure {@code t} is usually an {@link ApiException} carrying the
     * server message.
     */
    public interface Callback<T> {
        void onSuccess(T result);
        void onError(Throwable t);
    }

    // ---------------------------------------------------------------
    // Shared request core
    // ---------------------------------------------------------------

    /**
     * Fire one request and parse the JSON body into {@code responseType}.
     *
     * @param method       HttpMethods.GET / POST / PUT / DELETE
     * @param path         path AFTER {@link #BASE_URL} (must start with '/')
     * @param requestBody  object serialized to a JSON body, or null for none
     * @param responseType Gson type of the expected response (use a
     *                     TypeToken for List&lt;...&gt;)
     */
    private <T> void send(String method, String path, Object requestBody,
                          final Type responseType, final Callback<T> cb) {
        HttpRequestBuilder builder = new HttpRequestBuilder();
        HttpRequest req = builder
                .newRequest()
                .method(method)
                .url(BASE_URL + path)
                .header("Accept", "application/json")
                .build();

        if (requestBody != null) {
            req.setHeader("Content-Type", "application/json");
            req.setContent(gson.toJson(requestBody));
        }

        Gdx.net.sendHttpRequest(req, new HttpResponseListener() {
            @Override
            public void handleHttpResponse(HttpResponse response) {
                int status = response.getStatus().getStatusCode();
                String content = response.getResultAsString();
                if (status >= 200 && status < 300) {
                    try {
                        T parsed = responseType == null
                                ? null
                                : gson.<T>fromJson(content, responseType);
                        cb.onSuccess(parsed);
                    } catch (Exception parseError) {
                        cb.onError(parseError);
                    }
                } else {
                    // Backend sends a plain-text reason in the body.
                    String msg = (content == null || content.trim().isEmpty())
                            ? "Erro do servidor (HTTP " + status + ")"
                            : content.trim();
                    cb.onError(new ApiException(status, msg));
                }
            }

            @Override
            public void failed(Throwable t) {
                cb.onError(new ApiException(0,
                        "Não foi possível conectar ao servidor. Ele está rodando?"));
            }

            @Override
            public void cancelled() {
                cb.onError(new ApiException(0, "Requisição cancelada"));
            }
        });
    }

    private static Map<String, Object> body(Object... kv) {
        Map<String, Object> m = new HashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            m.put((String) kv[i], kv[i + 1]);
        }
        return m;
    }

    // ---------------------------------------------------------------
    // Account
    // ---------------------------------------------------------------

    /** POST /api/jogadores  body { "username", "senha" } -> created Jogador (201). */
    public void cadastrar(String username, String senha, Callback<Jogador> cb) {
        send(HttpMethods.POST, "/jogadores",
                body("username", username, "senha", senha),
                Jogador.class, cb);
    }

    /** POST /api/jogadores/login  body { "username", "senha" } -> Jogador (200) or 401. */
    public void login(String username, String senha, Callback<Jogador> cb) {
        send(HttpMethods.POST, "/jogadores/login",
                body("username", username, "senha", senha),
                Jogador.class, cb);
    }

    // ---------------------------------------------------------------
    // GETs
    // ---------------------------------------------------------------

    /** GET /api/loja -> List&lt;Movel&gt; (full catalog). */
    public void fetchCatalogoLoja(Callback<List<Movel>> cb) {
        Type listType = new TypeToken<List<Movel>>() { }.getType();
        send(HttpMethods.GET, "/loja", null, listType, cb);
    }

    /** GET /api/jogadores/ranking -> List&lt;Jogador&gt; (sorted DESC by tempoEstudado). */
    public void fetchRanking(Callback<List<Jogador>> cb) {
        Type listType = new TypeToken<List<Jogador>>() { }.getType();
        send(HttpMethods.GET, "/jogadores/ranking", null, listType, cb);
    }

    /** GET /api/jogadores/username/{username} -> Jogador (friend search). */
    public void fetchJogadorPorUsername(String username, Callback<Jogador> cb) {
        send(HttpMethods.GET, "/jogadores/username/" + encode(username),
                null, Jogador.class, cb);
    }

    /** GET /api/casas/jogador/{jogadorId} -> Casa */
    public void fetchCasaDoJogador(long jogadorId, Callback<Casa> cb) {
        send(HttpMethods.GET, "/casas/jogador/" + jogadorId,
                null, Casa.class, cb);
    }

    // ---------------------------------------------------------------
    // POSTs / PUTs (with body when applicable)
    // ---------------------------------------------------------------

    /** POST /api/sessoes  body { jogadorId, minutosConcluidos } -> SessaoEstudo. */
    public void registrarSessao(long jogadorId, int minutos, Callback<SessaoEstudo> cb) {
        send(HttpMethods.POST, "/sessoes",
                body("jogadorId", jogadorId, "minutosConcluidos", minutos),
                SessaoEstudo.class, cb);
    }

    /** POST /api/loja/comprar  body { jogadorId, movelId } -> updated Jogador. */
    public void comprarMovel(long jogadorId, long movelId, Callback<Jogador> cb) {
        send(HttpMethods.POST, "/loja/comprar",
                body("jogadorId", jogadorId, "movelId", movelId),
                Jogador.class, cb);
    }

    /** PUT /api/casas/slot/{slotId}/movel/{movelId} -> updated Casa. */
    public void colocarMovelNoSlot(long slotId, long movelId, Callback<Casa> cb) {
        send(HttpMethods.PUT, "/casas/slot/" + slotId + "/movel/" + movelId,
                null, Casa.class, cb);
    }

    /** POST /api/casas/{id}/like -> updated Casa. */
    public void darLike(long casaId, Callback<Casa> cb) {
        send(HttpMethods.POST, "/casas/" + casaId + "/like",
                null, Casa.class, cb);
    }

    private static String encode(String s) {
        try {
            return java.net.URLEncoder.encode(s, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            return s; // UTF-8 is always available; unreachable in practice.
        }
    }
}
