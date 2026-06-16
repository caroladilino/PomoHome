# Migration plan — immediate-mode → scene2d.ui

> Segmented so each step is **independently shippable** and leaves the game
> runnable. Execute top-to-bottom; stop at any checkpoint. Primary motivation:
> **resizing currently breaks the game completely** (fixed pixel offsets +
> manual `ShapeRenderer`/`SpriteBatch` projection in `TelaJogo`/`TelaVisita`).
> Segment 1 fixes that on its own — do it first even if nothing else follows.

## Current state (what exists)

- **Already scene2d:** `TelaLogin`, `TelaRanking`, `TelaAmigos` (use `Stage` +
  `UiSkin` + `Table`). These resize fine.
- **Immediate-mode (the problem):** `TelaJogo`, `TelaVisita` — `ShapeRenderer` +
  `SpriteBatch` + `BitmapFont`, hand-rolled `isClicado`, fixed offsets, manual
  `setToOrtho2D` on resize. Widgets in `core/.../ui/widgets/`
  (`Botao`, `CaixaTexto`, `EspacoMovel`, `CasaView`, `LojaView`, `InventarioView`).
- **Known UI debts to resolve along the way:**
  - **Two coin indicators on the main screen**: (a) the top-right pill *outside*
    the left menu, and (b) the gold `$N` *inside* the left panel. Consolidate to
    scene2d `Label`(s) and decide intentionally whether to keep the outside-menu
    one. (User flagged the extra one.)
  - Blurry text from `font.getData().setScale(3f)` — fix with FreeType.

## Design decisions (make once, up front)

- **Virtual resolution + viewport:** `ExtendViewport(1280, 720)`. Keeps a fixed
  world-unit size (so the **house never distorts/resizes** — satisfies "stipulate
  a limit so the home isn't resized") and extends on larger windows instead of
  letterboxing. World coords replace `Gdx.graphics.getWidth()/getHeight()` math.
- **Custom shapes inside scene2d** (iso grid, timer ring): use **ShapeDrawer**
  (draws through the `Batch`, no `ShapeRenderer`/`Batch` conflict). Alternative:
  diamond/furniture **textures** drawn with the `Batch` (preferred long-term once
  there's real art). Avoid the `batch.end()/sr.begin()` dance.
- **Fonts:** `gdx-freetype` — generate crisp `BitmapFont`s at real sizes (one
  ~18px body, one ~64px timer) instead of scaling the built-in font.

---

## Segment 0 — Dependencies + fonts (enabling, low risk) ✅ DONE

**Goal:** crisp text everywhere; pull in the libs the later segments need.

- `core/build.gradle`: add
  `api "com.badlogicgames.gdx:gdx-freetype:$gdxVersion"` and
  `implementation "space.earlygrey:shapedrawer:2.5.0"`.
- `lwjgl3/build.gradle`: add
  `implementation "com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-desktop"`.
- New `ui/Fonts.java` (or extend `UiSkin`): a `FreeTypeFontGenerator` producing
  `body` (~18) and `timer` (~64) `BitmapFont`s; register both in the `Skin`
  (`skin.add("default", bodyFont)`, `skin.add("timer", timerFont)`) and rebuild
  the `LabelStyle`/`TextButtonStyle`/`TextFieldStyle` to use `body`.
- A ShapeDrawer needs a 1×1 white `TextureRegion` — reuse the `UiSkin` "white"
  texture (`new ShapeDrawer(batch, skin.getRegion("white"))`).

**Checkpoint:** run; existing scene2d screens show crisper text. No behavior change.
**Risk:** native dependency wiring (freetype-platform). Verify `:lwjgl3:run` boots.

---

## Segment 1 — Viewport bridge for the immediate-mode screens (FIXES RESIZE) ✅ DONE

**Goal:** stop resize from breaking the game **without** rewriting `TelaJogo` yet.

- In `TelaJogo` (and `TelaVisita`): create an `ExtendViewport(1280,720)` with an
  `OrthographicCamera`. In `resize()` call `viewport.update(w, h, true)`.
- Each frame: `viewport.apply()`, then set
  `batch.setProjectionMatrix(camera.combined)` and
  `shapeRenderer.setProjectionMatrix(camera.combined)`.
- Replace every `Gdx.graphics.getWidth()/getHeight()` in `calcularLayout` with
  the **world size** (`viewport.getWorldWidth()/getWorldHeight()`, constant
  1280×720). Map mouse via `viewport.unproject(...)` instead of
  `h - Gdx.input.getY()`.

**Checkpoint:** resize the window any which way — UI scales/extends, house stays
fixed size, nothing breaks. This is the headline fix; safe to ship alone.
**Risk:** low. The layout math stays the same, just in world units.

---

## Segment 2 — Left panel + menu as scene2d ✅ DONE

> Done with two deliberate scoping notes: (a) the rounded pink/cream **panel
> background stays immediate-mode** (ShapeRenderer) to keep the rounded corners —
> scene2d buttons + coin `Label` are layered on top via a `Stage` sharing the
> viewport; (b) the **timer digits stay immediate-mode** (drawn with the bold
> font) until Segment 4 turns the ring into an actor — moving them now would mean
> absolutely-positioning a Label over an immediate-mode ring for no gain. Coin
> consolidated to a **single panel-header `Label`** (top-right pill removed) per
> the user's choice. Input is an `InputMultiplexer(stage, jogoInput, nameBox)`.

**Goal:** delete hand-rolled button hit-testing; real layout; fix the coin debt.

- Add a `Stage(viewport)` to `TelaJogo`. Build the **left pink panel** as a
  `Table` (NinePatch or tinted background): timer `Label` (style "timer"), and
  `TextButton`s INICIAR / EDITAR / LOJA / EDITAR CASA / RANKING / AMIGOS with
  `ChangeListener`s calling the existing methods. Use button `setVisible`/swapping
  for the state machine (PADRAO vs RODANDO vs EDITANDO) instead of manual draw
  branches.
- **Coin indicators:** make both a single `Label` updated each frame from
  `jogador.getSaldo()`. Decide: keep **one** canonical coin `Label` in the panel
  header, and either remove the top-right pill or re-add it deliberately as a
  second `Label` in a top-right `Table`. (Resolves the "another money indication
  outside the menu" note.)
- Keep the ring + grid drawn via ShapeDrawer in a thin render pass under
  `stage.draw()` for now (or already as actors if doing Segment 4 next).
- Input: `Gdx.input.setInputProcessor(stage)`; the grid still polls or gets its
  own actor later.

**Checkpoint:** menu fully scene2d; timer/loja/edit/nav all work via buttons;
exactly one (or one intentional pair of) coin label(s).
**Risk:** medium — the timer state machine moves from draw-branches to actor
visibility. Re-test INICIAR/PAUSAR/EDITAR/+/-/CANCELAR.

---

## Segment 3 — Store + inventory as scene2d ✅ DONE

> Both are now a `ScrollPane` of a 2-column `Table` of clickable item cells, shown
> inside the panel and rebuilt on demand (catalog refresh / purchase / pick /
> place / remove). `LojaView` + `InventarioView` deleted. Pick/drop kept the
> click-to-pick → click-grid model: while an item is "in hand" the inventory
> scroll's `Touchable` is disabled so clicks fall through to the grid (place) or
> panel (drop back). Full DragAndDrop is deferred to Segment 4 with the grid actor.

**Goal:** convert the two left-panel overlays; remove `LojaView`/`InventarioView`.

- **Loja:** a `Table`/`ScrollPane` (or grid of `ImageButton`s) built from
  `jogo.getLoja().getItensDisponiveis()`; price `Label` red if unaffordable; click
  → `comprar(...)`. Rebuild the table on catalog refresh + after a purchase
  (`Gdx.app.postRunnable`).
- **Inventory (edit mode):** same idea from `inventarioExibido(jogador)`; the
  "EDITAR CASA" button shows this table, "FECHAR" hides it + saves the layout.
- **Pick/drop:** simplest is keep click-to-pick → click grid. Better: scene2d
  `DragAndDrop` (Segment 4 makes the grid a drop target).

**Checkpoint:** buying + inventory listing fully scene2d. `LojaView`/
`InventarioView` deletable.
**Risk:** medium — re-verify the owned−placed derivation feeding the inventory.

---

## Segment 4 — House grid + timer ring as custom Actors ✅ DONE

> Split into **4a** and **4b**, both done:
> - **4a ✅ DONE — TimerRingActor.** The Pomodoro ring + MM:SS are now a scene2d
>   `Actor` drawn via **ShapeDrawer** (through the Stage's `PolygonSpriteBatch`),
>   wiring up ShapeDrawer for 4b. Immediate-mode ring/timer drawing removed. The
>   progress fill is a `filledTriangle` fan (ShapeDrawer's thick `arc`/`sector`
>   rendered unreliably) over a grey track disc, carved by a cream inner disc.
> - **4b ✅ DONE — CasaActor.** House grid is now `ui/actors/CasaActor` (ShapeDrawer
>   diamonds + furniture names, geometry recomputed in world coords each `draw`).
>   The house-name `CaixaTexto` → scene2d `TextField` (editable only in edit mode,
>   disabled-but-visible otherwise); the X-remove `Botao` → scene2d `TextButton`
>   floated over the selected tile. **Click-to-place kept** (user's choice): the
>   actor is `Touchable.disabled` and the screen's `jogoInput` routes grid clicks
>   to `selecionarSob`/`tentarColocar` (so panel-drop still works); the inventory
>   cell click picks up, the "X" removes. `TelaVisita` rebuilt as scene2d
>   (read-only `CasaActor` + `Label`s + `TextButton`s) — this **folds in Segment
>   5**. The old `CasaView`, `EspacoMovel`, `Botao`, `CaixaTexto` are deleted.

**Goal:** the genuinely custom rendering, now inside the Stage.

- `CasaActor extends Actor`: holds the model `Casa`; `draw(Batch, float)` renders
  the 8×8 iso diamonds + placed furniture (ShapeDrawer or tile textures);
  `hit(x,y,touchable)` reuses the `Polygon.contains` test on **actor-local**
  coords. Move the placement/removal/rename logic out of `CasaView` into here.
- `DragAndDrop`: inventory items = sources, `CasaActor` = target; on drop, run the
  multi-tile fit check + `casa.colocar(tile, movel)`. Replaces `movelNaMao`.
- Remove-placed: a small `X` `Button` actor positioned over the selected tile, or
  a right-click handler.
- `TimerRingActor`: draws the grey ring + progress arc (ShapeDrawer `arc`) with
  the timer `Label` centered (a child or overlaid `Label`).
- Name editing: replace `CaixaTexto` with a scene2d `TextField`.

**Checkpoint:** grid + ring are real actors; drag-and-drop placement works;
`Botao`, `CaixaTexto`, `EspacoMovel`, `CasaView` deletable.
**Risk:** highest — coordinate spaces + drag-and-drop + multi-tile. Test
placement at grid edges, multi-tile overlap rejection, save/reload round-trip.

---

## Segment 5 — TelaVisita as scene2d ✅ DONE (folded into 4b)

**Goal:** friend-house view reuses `CasaActor` (read-only).

- `Stage(viewport)` + a read-only `CasaActor` (no drag, no remove) from the
  fetched `Casa`; `Label` for name + `numLikes`; `TextButton`s LIKE / VOLTAR.

Done as part of 4b: `TelaVisita` is now a `Stage` + read-only `CasaActor`
(`editavel=false`) + `Label`s + pink `TextButton`s, with the like toggle intact.

**Checkpoint:** visiting is scene2d and resizes cleanly.
**Risk:** low (read-only reuse of Segment 4).

---

## Segment 6 — Cleanup ✅ DONE

- ✅ `ui/widgets/*` already gone (removed across 3/4b). The last `ShapeRenderer` +
  `SpriteBatch` pass — the rounded pink panel, the held-item cursor square, and
  the panel title / feedback text in `TelaJogo` — is removed: the panel and cursor
  are now `ShapeDrawer` `Actor`s drawn through the Stage's batch, and the two text
  draws are scene2d `Label`s. No manual `camera.combined` projection remains;
  `render()` is just `calcularLayout` → logic → `stage.act/draw`.
- ✅ Single shared `Skin`/font lifecycle in `Main` (built once in `create()`,
  `skin.dispose()` once in `dispose()`; the cached `TelaJogo` disposed once too).
  `TelaJogo.dispose()` now only frees its Stage (which owns its
  `PolygonSpriteBatch`); fonts/white region belong to the Skin.
- ✅ Panel styling: kept the rounded corners via a `ShapeDrawer` rounded-rect
  helper (`retanguloArredondado`) rather than a NinePatch asset — consistent with
  the all-programmatic `UiSkin` (no atlas/.fnt files in the project).
- Final resize test at 1024×640 (min) and a large/ultrawide window — **user-tested**.
- ✅ `CLAUDE.md` / `SETUP.MD` updated to say the UI is scene2d throughout.

> Remaining `ShapeDrawer` use (timer ring, house diamonds, panel, cursor) is the
> intended end state — ShapeDrawer draws through the Stage's batch, so it's part
> of the scene2d pipeline, not a separate immediate-mode pass.

---

## Suggested order if you can't do it all

1. **Segment 0 + 1** — crisp fonts + the resize fix. Highest value, lowest risk.
   If you stop here, the reported bug is gone.
2. **Segment 2** — menu + coin-label consolidation (kills most hand-rolled code).
3. **Segments 3–6** — only if full consistency/maintainability is the goal.

## Per-segment definition of done

Build (`./gradlew :lwjgl3:run`, backend up first) → the segment's feature works →
**resize the window small and large** → no layout break, house keeps its size.
