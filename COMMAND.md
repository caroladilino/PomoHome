# Command Pattern — house-editor undo/redo

> GoF Behavioral pattern, added to the LibGDX client (`core/`) in `ui/comandos/`.
> It is independent of the other two project patterns ([State](STATE.md) and
> [Observer](OBSERVER.md), which handle the timer lifecycle).

## Motive
In edit mode the player drops furniture on the 8×8 grid and removes it with the
"X" button. Those mutations were applied imperatively
(`casaActor.tentarColocar(...)`, `casaActor.removerSelecionado()`) with no way to
undo them — a gap for a decoration game.

## Function
Every edit becomes an object that knows how to do **and** undo itself. An invoker
keeps undo/redo stacks, so the player can step backward and forward through their
edits.

## Implementation
GoF roles → classes:

- **Command** → `ComandoEdicao` interface: `boolean executar()` / `void desfazer()`.
- **Concrete Commands** → `ComandoColocar` (place a móvel; undo removes that same
  instance) and `ComandoRemover` (captures the selected móvel + its anchor tile at
  construction; undo re-places it exactly).
- **Invoker** → `GerenciadorComandos`: `executar` runs a command and, on success,
  pushes it to the undo stack and clears the redo stack; `desfazer`/`refazer` move
  commands between the two stacks; `podeDesfazer`/`podeRefazer` drive the buttons;
  `limpar` resets the history.
- **Receiver** → `CasaActor` (the grid). It gained tile-aware operations so edits
  are reversible: `tentarColocar` now **returns the anchor tile** (null on
  failure) instead of a boolean, plus `removerInstancia(Movel)`,
  `recolocarEm(tile, Movel)`, `movelSelecionado()`, `tileSelecionadoAncora()`. The
  dead `removerSelecionado()` was deleted.

### Flow
```
place click ─▶ GerenciadorComandos.executar(new ComandoColocar(casaActor, movel, x, y))
                   ├▶ comando.executar()  → CasaActor.tentarColocar(...) returns anchor tile
                   └▶ push to undo stack, clear redo stack
DESFAZER    ─▶ GerenciadorComandos.desfazer()
                   ├▶ comando.desfazer() → CasaActor.removerInstancia(movel)
                   └▶ move command to redo stack
REFAZER     ─▶ GerenciadorComandos.refazer()  (re-executes, back to undo stack)
```

The inventory list is derived from `owned − placed − in-hand`, so undo/redo need
no extra bookkeeping: after any command the screen just calls
`reconstruirInventario()` and the piece reappears/disappears correctly.

## Package note
These live in `ui/comandos/`, **not** `model/comandos/` (as an early draft
proposed): the commands operate on `CasaActor`, a UI actor, so keeping them in the
UI layer avoids the model depending on the UI.

## Where the modifications are
- **New:** `ui/comandos/ComandoEdicao.java`, `ComandoColocar.java`,
  `ComandoRemover.java`, `GerenciadorComandos.java`.
- **`ui/actors/CasaActor.java`:** the tile-aware methods above.
- **`screens/TelaJogo.java`:** holds a `GerenciadorComandos`; placement
  (`tratarCliqueInventario`) and removal (the "X" handler) go through it; added
  DESFAZER/REFAZER buttons (created in `criarChromeEdicao`, positioned in
  `layoutCasa`, enabled/disabled in `atualizarBotoes`); `abrirInventario` calls
  `comandos.limpar()` so undo history is per edit session.

## Verification
- Type-check: `./gradlew :core:compileJava`.
- Manual (backend up, then `./gradlew :lwjgl3:run`): in EDITAR CASA, place a piece
  then DESFAZER (returns to inventory) and REFAZER (comes back); remove with X then
  DESFAZER (returns to the grid); buttons disable at the ends of history; FECHAR
  still saves the layout.
