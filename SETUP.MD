# PomoHome — Running the Stack & Frontend ↔ API Guide

PomoHome is two separate processes: the **Spring Boot backend** (REST API on
`:8080`) and the **LibGDX game client**. The game does **not** start the
server — you run both yourself, backend first.

## Prerequisites

- **JDK 17** available for the `backend/` subproject. The other subprojects
  build on Java 8; Gradle's toolchain handles the split. If a JDK 17 isn't
  found, let Gradle auto-provision it (`org.gradle.java.installations.auto-download`).
- Nothing else — H2 is embedded, no database to install.
- Use the bundled `./gradlew` (Linux/macOS) or `gradlew.bat` (Windows).

## 1. Start the backend

```bash
./gradlew :backend:bootRun
```

Server is up when the log shows Tomcat on port 8080. Verify:

```bash
curl -i -X POST http://localhost:8080/api/jogadores \
  -H 'Content-Type: application/json' \
  -d '{"username":"teste","senha":"1234"}'
# → HTTP 201 + JogadorDTO JSON (note: no "senha" field)
```

Inspect the database while it runs at <http://localhost:8080/h2-console>:
JDBC URL `jdbc:h2:file:./data/pomohome`, user `sa`, blank password.

Data persists in `backend/data/pomohome.mv.db`. To wipe and regenerate the
schema, stop the server and delete that file (required after any entity
**column rename** — `ddl-auto=update` won't migrate it).

## 2. Start the game

In a **second terminal** (leave the backend running):

```bash
./gradlew :lwjgl3:run
```

The game opens on `TelaLogin`. Enter a username/password and **Entrar**
(login) or **Criar conta** (register). On success you land on `TelaJogo` — the
main game screen: Pomodoro timer (INICIAR/EDITAR/LOJA), an 8×8 isometric house,
a collapsible inventory on the right, and RANKING/AMIGOS buttons. If the backend
is down you'll see *"Não foi possível conectar ao servidor"* — start step 1.

**Edit mode:** opening the inventory (the `<` tab on the right) puts the house in
edit mode — drag furniture onto the grid; the timer is locked until you close the
inventory, which saves the layout to the server.

> New accounts start with an **empty house**. The seeded store catalog appears
> automatically (the backend seeds `MOVEL` on first boot). If you have an old H2
> database from before this change and see leftover sofa/mesa/cama slots, delete
> `backend/data/pomohome.mv.db` once and let it regenerate.

### Other useful commands

```bash
./gradlew projects             # confirm core, lwjgl3, backend are registered
./gradlew :backend:compileJava # type-check the backend without running it
./gradlew clean                # nuke build outputs
```

## How the frontend talks to the API

All HTTP lives in **one class**: `core/.../network/ApiClient.java`. Screens
never call `Gdx.net` directly — they call typed methods on `ApiClient`.

### The shape

- **Base URL:** `http://localhost:8080/api` (constant `ApiClient.BASE_URL` —
  change there if the server moves).
- **One instance, owned by `Main`.** `Main extends Game` creates the
  `ApiClient`, the in-memory `Jogo` session, and the shared `Skin` in
  `create()`. Screens receive `Main` in their constructor and read these via
  getters (`main.getApiClient()`).
- **JSON:** Gson. **Frontend `model.*` field names must match backend DTO
  field names exactly** — Gson maps by reflection, so renaming one side
  requires renaming the other.

### Calling a method

Every call is **asynchronous** and takes an `ApiClient.Callback<T>`:

```java
api.login(usuario, senha, new ApiClient.Callback<Jogador>() {
    @Override public void onSuccess(Jogador jogador) {
        Gdx.app.postRunnable(() -> main.entrarNoJogo(jogador));
    }
    @Override public void onError(Throwable t) {
        Gdx.app.postRunnable(() -> mostrarErro(t.getMessage()));
    }
});
```

> **CRITICAL threading rule:** `Gdx.net.sendHttpRequest` runs the callback on
> a **background thread**, not the GL/render thread. Any UI mutation (labels,
> screen switch, anything read by `render`) inside `onSuccess`/`onError`
> **must** be wrapped in `Gdx.app.postRunnable(...)`. `ApiClient` only does
> network + JSON; hopping back to the render thread is the screen's job.

### Error handling

Non-2xx responses become an `ApiException` whose **message is the server's
plain-text body** (set by the backend `GlobalExceptionHandler`), so it can be
shown to the user verbatim:

| Situation | `ApiException` | Example message shown |
|---|---|---|
| Validation (400) | `getStatus() == 400` | `Username já existe`, `Saldo insuficiente` |
| Bad credentials (401) | `getStatus() == 401` | `Credenciais inválidas` |
| Not found (404) | `getStatus() == 404` | `Recurso não encontrado` |
| Server unreachable / cancelled | `getStatus() == 0` | `Não foi possível conectar ao servidor. Ele está rodando?` |

`status == 0` means the request never reached the server (backend down) — a
distinct case from a real HTTP error.

### Available endpoints (via `ApiClient`)

| Method | HTTP | Returns |
|---|---|---|
| `cadastrar(username, senha, cb)` | `POST /jogadores` | `Jogador` (201) |
| `login(username, senha, cb)` | `POST /jogadores/login` | `Jogador` (200/401) |
| `fetchCatalogoLoja(cb)` | `GET /loja` | `List<Movel>` |
| `fetchRanking(cb)` | `GET /jogadores/ranking` | `List<Jogador>` (desc by `tempoEstudado`) |
| `fetchJogadorPorUsername(username, cb)` | `GET /jogadores/username/{username}` | `Jogador` (friend search) |
| `fetchCasaDoJogador(jogadorId, cb)` | `GET /casas/jogador/{id}` | `Casa` |
| `registrarSessao(jogadorId, minutos, cb)` | `POST /sessoes` | `SessaoEstudo` |
| `comprarMovel(jogadorId, movelId, cb)` | `POST /loja/comprar` | updated `Jogador` |
| `colocarMovelNoSlot(slotId, movelId, cb)` | `PUT /casas/slot/{slotId}/movel/{movelId}` | updated `Casa` (legacy; unused by the free grid) |
| `salvarLayoutCasa(casaId, placements, cb)` | `PUT /casas/{id}/layout` | updated `Casa` (whole-house save) |
| `darLike(casaId, jogadorId, cb)` | `POST /casas/{id}/like?jogadorId=…` | updated `Casa` (toggles the visitor's like; one per visitor) |
| `fetchJogadorPorId(id, cb)` | `GET /jogadores/{id}` | `Jogador` (refresh / resolve a friend) |
| `enviarSolicitacao(remetenteId, destinatarioId, cb)` | `POST /solicitacoes?…` | `SolicitacaoAmizade` |
| `aceitarSolicitacao(id, cb)` / `recusarSolicitacao(id, cb)` | `POST /solicitacoes/{id}/aceitar`\|`/recusar` | `SolicitacaoAmizade` |
| `listarSolicitacoesRecebidas(jogadorId, cb)` | `GET /solicitacoes/recebidas/{id}` | `List<SolicitacaoAmizade>` |
| `removerAmigo(jogadorId, amigoId, cb)` | `DELETE /solicitacoes/amizade/{jogadorId}/{amigoId}` | 204 |

### Admin / debug endpoints (curl-only, not in `ApiClient`)

Handy for seeding a test account; not used by the game client:

| HTTP | Effect |
|---|---|
| `POST /api/jogadores/{id}/creditar?valor=N` | Add N coins (does **not** touch `tempoEstudado`/ranking) |
| `DELETE /api/jogadores/{id}/inventario` | Empty the player's furniture inventory |
| `POST /api/loja` (body: a `Movel` JSON) | Add a catalog item |
| `DELETE /api/loja/{id}` | Remove a catalog item |

Note: each catalog móvel can be bought **at most once** per player — `comprarMovel`
returns 400 `Móvel já comprado` on a repeat, and the store hides owned items.

### Where the logged-in player lives

On a successful login/register, `TelaLogin` hands the `Jogador` to
`Main.entrarNoJogo(jogador)`, which stores it in the in-memory `Jogo` session
(`jogo.setJogadorLogado(...)`) and switches to `TelaPrincipal`. There is no
persistent login yet — every launch starts at `TelaLogin`.

