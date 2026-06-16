# State Pattern — the Pomodoro timer lifecycle

> GoF Behavioral pattern, added to the LibGDX client (`core/`) in `model/timer/`.
> It works together with the [Observer](OBSERVER.md) pattern: the running state
> detects cycle completion and publishes it. The [Command](COMMAND.md) pattern is
> the third pattern in the project (house-editor undo/redo).

## Motive
The timer had four modes — idle, editing the cycle length, running, paused — held
in `TelaJogo` as `private enum EstadoTimer { PADRAO, EDITANDO, RODANDO, PAUSADO }`.
The behavior for each mode was spread across the screen as conditionals:
`acaoEsquerda`/`acaoDireita` (the buttons), `correndo()`, `atualizarLogica` (the
per-frame tick), and `atualizarBotoes` (which buttons show and what they say).
Adding or changing a mode meant editing **every** one of those methods — the
textbook State smell.

## Function
Each mode becomes a class that owns (a) its transitions and (b) the UI rules for
that mode. The screen no longer asks "which state am I in?"; it delegates the
action to the current state and reads back what to display.

## Implementation
GoF roles → classes:

- **Context** → `ContextoTimer`. Composes the unchanged `Timer` model and the
  current `EstadoTimer`. Exposes the four actions (`iniciarOuPausar`,
  `editarOuCancelar`, `aceitar`, `atualizar(delta)`) which it forwards to the
  current state, plus `transicionar(EstadoTimer)` that the states call to move on.
- **State** → `EstadoTimer` interface. Action methods default to no-ops (so each
  state overrides only the transitions it allows) plus the UI-query methods
  `textoBotaoEsquerdo/Direito`, `mostraIniciarEditar`, `mostraNavegacao`,
  `mostraControlesEdicao`.
- **Concrete States** → `EstadoParado`, `EstadoRodando`, `EstadoPausado`,
  `EstadoEditando`. Examples:
  - `EstadoParado.iniciarOuPausar` starts the timer and transitions to
    `EstadoRodando`; its `atualizar` refills a finished cycle (the old
    "reset on zero" line).
  - `EstadoRodando.atualizar` ticks the `Timer`; on the frame it hits zero it
    transitions to `EstadoParado` and fires the Observer (see [OBSERVER.md](OBSERVER.md)).
  - `EstadoEditando.aceitar` returns to `EstadoParado`; the +/− buttons keep
    editing the cycle length directly (config, not a transition).

### State transitions

```
                 iniciarOuPausar                 iniciarOuPausar
   EstadoParado ───────────────▶ EstadoRodando ◀───────────────▶ EstadoPausado
        ▲ │                          │   │       iniciarOuPausar       │
        │ │ editarOuCancelar         │   │ atualizar() hits 0          │
        │ ▼                          │   └─▶ EstadoParado + notify      │
   EstadoEditando                    │                                  │
        │  aceitar / editarOuCancelar└──────── editarOuCancelar (reset)─┘
        └──────────────▶ EstadoParado            ─▶ EstadoParado
```

## Where the modifications are
- **New:** `model/timer/EstadoTimer.java`, `EstadoParado.java`, `EstadoRodando.java`,
  `EstadoPausado.java`, `EstadoEditando.java`, `ContextoTimer.java`.
- **`model/Jogo.java`:** holds a `ContextoTimer` instead of a `Timer`; adds
  `getContextoTimer()`; `getTimer()` now delegates to `contextoTimer.getTimer()`
  so `TimerRingActor` is untouched.
- **`screens/TelaJogo.java`:** deleted the `EstadoTimer` enum, the `estadoAtual`
  field, and `acaoEsquerda/acaoDireita/correndo()`. Buttons delegate
  (`btnEsq → ctx::iniciarOuPausar`, etc.); `atualizarLogica` calls
  `ctx.atualizar(delta)`; `atualizarBotoes` reads `ctx.getEstado().mostra…()`.

## Verification
- Type-check: `./gradlew :core:compileJava`.
- Manual (backend up, then `./gradlew :lwjgl3:run`): INICIAR → PAUSAR/RETOMAR →
  EDITAR (±) → ACEITAR → CANCELAR behave as before; button labels and visibility
  match each state.
