# Observer Pattern — reacting to a completed cycle

> GoF Behavioral pattern, added to the LibGDX client (`core/`) in `model/timer/`.
> It works together with the [State](STATE.md) pattern: the running state detects
> cycle completion and publishes it through this Observer. The [Command](COMMAND.md)
> pattern is the third pattern in the project (house-editor undo/redo).

## Motive
Cycle completion was polled in the render loop: `timer.atualizar(delta)` returned
`true` for one frame and `onPomodoroCompleto` then hard-wired the consequences
(register the session, re-read the balance, show feedback). The timer was coupled
to that one screen, and any new consequence (history refresh, achievements) could
only be bolted into the same method.

## Function
The timer announces "a cycle finished" and whoever cares subscribes. The timer
no longer knows who reacts, and new reactions are added without touching it.

## Implementation
GoF roles → classes:

- **Subject** → `ContextoTimer` (the same context that drives the
  [State](STATE.md) machine). Keeps a list of listeners
  (`adicionarOuvinte`/`removerOuvinte`) and `notificarConclusao(int minutos)`.
- **Observer** → `OuvinteSessao` interface: `void cicloConcluido(int minutos)`.
- **Concrete Observer** → `TelaJogo::onPomodoroCompleto`, registered as a method
  reference.
- **Notification trigger** → `EstadoRodando.atualizar` calls
  `ctx.notificarConclusao(timer.minutosDoCiclo())` — so State and Observer meet
  here: the running state detects completion and publishes it.

### Flow
```
render loop ─▶ ContextoTimer.atualizar(delta)
                   └▶ EstadoRodando.atualizar()  (Timer hits 0)
                         ├▶ transicionar(EstadoParado)     [State]
                         └▶ ContextoTimer.notificarConclusao(minutos)
                                └▶ OuvinteSessao.cicloConcluido(minutos)
                                      └▶ TelaJogo.onPomodoroCompleto()
                                            └▶ api.registrarSessao(...) → refresh saldo
```

Registration happens **once in the `TelaJogo` constructor**, not in `show()`,
because `Main` caches and reuses the screen — subscribing in `show()` would add a
duplicate listener every time the player returns from Ranking/Friends/History.
Each UI mutation inside the callback is still wrapped in `Gdx.app.postRunnable`
(the `ApiClient` callbacks run off the render thread).

## Where the modifications are
- **New:** `model/timer/OuvinteSessao.java`; subject methods added to
  `ContextoTimer.java`; the notify call lives in `EstadoRodando.java`.
- **`screens/TelaJogo.java`:** constructor does
  `ctx.adicionarOuvinte(this::onPomodoroCompleto)`; `onPomodoroCompleto` changed
  from `(Jogador, Timer)` to `(int minutos)` (it reads the logged-in player
  itself) and no longer flips any state flag. `atualizarLogica` no longer polls
  for completion.

## Extensibility
New reactions subscribe without touching the timer — e.g. a history-refresh
listener or an achievements/streak listener can call `ctx.adicionarOuvinte(...)`
independently.

## Verification
- Type-check: `./gradlew :core:compileJava`.
- Manual (backend up, then `./gradlew :lwjgl3:run`): finishing a cycle still
  credits coins and shows the "+N moedas" feedback (proves the listener fired).
