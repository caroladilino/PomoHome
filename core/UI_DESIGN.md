# PomoHome — UI Design & Color System

> Frontend-only (`core/`). The single source of truth for color is
> [`io.github.PomoHome.ui.Palette`](src/main/java/io/github/PomoHome/ui/Palette.java).
> **Never hard-code a hex value in a screen or actor — add it to `Palette` and
> reference it from there.** This keeps every screen in harmony and lets us
> re-theme the whole game from one file.

## The mood: "Cozy Focus"

PomoHome is a study/home game built on the Pomodoro technique. The look is warm
and calm so it's pleasant to sit in front of for long focus sessions, with one
energetic accent (terracotta) that nods to the Pomodoro tomato.

The palette is built from five roles:

| Role | Color | Hex | Where it's used |
|------|-------|-----|-----------------|
| **Background** | Deep indigo | `#20212B` | Behind every screen (`ScreenUtils.clear`) |
| **Surface** | Warm parchment | `#F2EAD8` | Game panel, timer inner disc |
| **Primary accent** | Terracotta | `#E07A5F` | Timer progress arc, primary emphasis |
| **Action** | Sage green | `#81B29A` | All buttons |
| **Highlight** | Soft gold | `#E9C46A` | Coins, tile selection, "this is me" in ranking |

These four hues (indigo / parchment / terracotta / sage) sit at comfortable
distances on the wheel: terracotta (warm orange-red) and sage (muted green) are
near-complementary, parchment is a desaturated tint of the terracotta family,
and indigo is the cool anchor that makes the warm tones pop. Gold bridges
parchment and terracotta for highlights.

## Full token list

All tokens live in `Palette`. Grouped by purpose:

### Backgrounds & surfaces
- `FUNDO` `#20212B` — deep indigo screen background.
- `PERGAMINHO` `#F2EAD8` — parchment surface (panel, timer core).
- `PERGAMINHO_BORDA` `#D8CBB0` — soft taupe panel outline. **This replaced the
  old pink panel border** — a darker shade of the parchment that defines the
  edge without shouting.
- `CELULA_LOJA` `#CDBFA6` / `CELULA_INV` `#D6CDBC` — store / inventory cell tints.

### Accents
- `TERRACOTA` `#E07A5F` (+ `TERRACOTA_PRESS` `#C75D43`) — the Pomodoro energy.
- `SAGE` `#81B29A` (+ `SAGE_PRESS` `#6A9A82`, `SAGE_HOVER` `#74A88D`) — buttons.
- `OURO` `#E9C46A` — highlight; `OURO_TEXTO` `#B07D2B` — gold that reads as text
  on parchment (the coin balance).

### Text & status
- `TEXTO_ESCURO` `#2B2D42` — text on parchment.
- `TEXTO_CLARO` `#F2EAD8` — text on the dark background.
- `ERRO` `#E06C75` · `SUCESSO` `#81B29A` · `NEUTRO` `#B8B8BE` — status messages.
- `DESABILITADO` / `DESABILITADO_TEXTO` — disabled buttons.

### House grid (`CasaActor`)
- `TILE_VAZIO` `#4A5568` · `TILE_OCUPADO` `#8B9BB4` · `TILE_CONTORNO` `#2B2D42` ·
  `TILE_SELECAO` = `OURO`.

### Timer ring (`TimerRingActor`)
- `ANEL_FUNDO` `#C9B79C` (taupe track) · `ANEL_PROGRESSO` = `TERRACOTA` (elapsed
  fill) · `ANEL_MIOLO` = `PERGAMINHO` (centre).

### Text fields (`UiSkin`)
- `CAMPO_FUNDO` (white @ 10%) · `CAMPO_SELECAO` (sage @ 40%).

## Where colors are applied

- **`UiSkin`** — builds the scene2d `Skin`: button, label, text-field, scroll-pane
  styles. Buttons are sage with dark parchment text. The `"rosa"` style key is
  **historical** — it now points at the same sage style as `"default"`; there is
  no pink left in the UI. (Kept only so `TelaJogo` / `TelaVisita` call sites
  don't break.)
- **`PainelActor`** — parchment panel + taupe outline.
- **`TimerRingActor`** — taupe track, terracotta progress, parchment core.
- **`CasaActor` / `CursorMovelActor`** — the isometric house tiles + held item.
- **Screens** (`TelaLogin`, `TelaJogo`, `TelaRanking`, `TelaAmigos`,
  `TelaVisita`) — background clear + label/status colors.

## Rules for future work

1. **Add new colors to `Palette`, reference by name.** No raw hex in screens/actors.
2. **Pick by role, not by hue.** Need a button? Use the sage tokens. A warning?
   `ERRO`. An emphasis/progress element? `TERRACOTA`. A reward/highlight? `OURO`.
3. **Contrast pairing:** dark text (`TEXTO_ESCURO`) on parchment/light surfaces,
   light text (`TEXTO_CLARO`) on the indigo background. Don't put light text on
   parchment or dark text on indigo.
4. **One energetic accent.** Terracotta is the spark — keep it for the timer and
   genuine emphasis. If everything is terracotta, nothing is.
5. **Re-theming** the whole game = editing `Palette` only. If you find yourself
   editing colors in more than one file for a single visual change, something
   leaked out of `Palette` — pull it back in.
