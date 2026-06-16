package io.github.PomoHome.ui.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import io.github.PomoHome.model.Casa;
import io.github.PomoHome.model.Movel;
import io.github.PomoHome.ui.Palette;
import space.earlygrey.shapedrawer.ShapeDrawer;

/**
 * The 8×8 isometric house as a scene2d {@link Actor}, drawn through a
 * {@link ShapeDrawer} (so it shares the Stage's batch — no separate
 * {@code ShapeRenderer}/projection pass). Replaces the immediate-mode
 * {@code CasaView} + {@code EspacoMovel} widgets.
 *
 * <p>Geometry is recomputed every {@link #draw} in <b>world</b> coordinates
 * (origin = the actor's {@code x,y}; the diamond is scaled to fit the actor's
 * bounds but never grows past its design size, so the house keeps a fixed look
 * and only shrinks on small windows). Hit-testing therefore takes world-space
 * mouse coordinates directly (what {@code viewport.unproject} yields).
 *
 * <p>The actor is {@link Touchable#disabled} for the Stage: clicks are routed by
 * the screen's own input handler (click-to-place was kept over drag-and-drop),
 * which calls {@link #selecionarSob}/{@link #tentarColocar}/{@link #removerInstancia}.
 * Placement state mirrors into {@link Casa#getPlacements()} (keyed by anchor tile
 * "L{row}C{col}") so it round-trips through the backend layout endpoint.
 *
 * <p>When {@code editavel} is false (visiting a friend) the grid is read-only:
 * no selection highlight, and placement/removal are never invoked.
 */
public class CasaActor extends Actor {

    public static final int MATRIZ = 8;

    private static final Color VAZIO = Palette.TILE_VAZIO;
    private static final Color OCUPADO = Palette.TILE_OCUPADO;
    private static final Color CONTORNO = Palette.TILE_CONTORNO;
    private static final Color SELECAO = Palette.TILE_SELECAO;

    private final Casa casa;
    private final boolean editavel;
    private final ShapeDrawer drawer;
    private final BitmapFont fonte;
    private final GlyphLayout layout = new GlyphLayout();

    // Per-tile state, indexed row*MATRIZ + col.
    private final Polygon[] poligonos = new Polygon[MATRIZ * MATRIZ];
    private final float[] centroX = new float[MATRIZ * MATRIZ];
    private final float[] centroY = new float[MATRIZ * MATRIZ];
    private final Movel[] ocupacao = new Movel[MATRIZ * MATRIZ];
    private final boolean[] anchor = new boolean[MATRIZ * MATRIZ];
    private int selecionado = -1;

    // Grid metrics from the last recalcular() (world units) — for name/X anchors
    // and the drop shadow.
    private float originX, originY, tileW, tileH;

    public CasaActor(Casa casa, boolean editavel, ShapeDrawer drawer, BitmapFont fonte) {
        this.casa = casa;
        this.editavel = editavel;
        this.drawer = drawer;
        this.fonte = fonte;
        for (int i = 0; i < poligonos.length; i++) {
            poligonos[i] = new Polygon(new float[8]);
        }
        setTouchable(Touchable.disabled); // the screen routes clicks, not the Stage
        reconstruirDoModelo();
    }

    // ---------------------------------------------------------------
    // Model <-> grid
    // ---------------------------------------------------------------

    /** Rebuild the visual occupancy from the model's placement map (call after load). */
    public void reconstruirDoModelo() {
        for (int i = 0; i < ocupacao.length; i++) {
            ocupacao[i] = null;
            anchor[i] = false;
        }
        selecionado = -1;
        if (casa == null) {
            return;
        }
        casa.getPlacements().forEach((tileName, movel) -> {
            int[] rc = parseTile(tileName);
            if (rc != null && movel != null) {
                ocuparArea(rc[0], rc[1], movel);
            }
        });
    }

    public void limparSelecao() {
        selecionado = -1;
    }

    /**
     * Select the (occupied) tile under the mouse, or clear the selection if the
     * click misses every tile or hits an empty one. World coordinates in.
     */
    public void selecionarSob(float worldX, float worldY) {
        if (!editavel) {
            return;
        }
        for (int i = poligonos.length - 1; i >= 0; i--) {
            if (poligonos[i].contains(worldX, worldY)) {
                selecionado = ocupacao[i] != null ? i : -1;
                return;
            }
        }
        selecionado = -1;
    }

    /**
     * Remove a specific móvel instance (anchor + footprint) by identity, both
     * from the visual grid and the model. Returns true if it was present. Used to
     * apply/undo the {@code ComandoRemover}/{@code ComandoColocar} edits.
     */
    public boolean removerInstancia(Movel m) {
        if (!editavel || m == null) {
            return false;
        }
        if (casa != null) {
            casa.removerMovel(m);
        }
        boolean encontrado = false;
        for (int i = 0; i < ocupacao.length; i++) {
            if (ocupacao[i] == m) {
                ocupacao[i] = null;
                anchor[i] = false;
                encontrado = true;
            }
        }
        selecionado = -1;
        return encontrado;
    }

    /**
     * Try to drop {@code m} under the mouse (world coords). Returns the anchor
     * tile name it landed on ("L{row}C{col}"), or {@code null} if it could not be
     * placed (off-grid or overlapping). The tile name lets a command undo it.
     */
    public String tentarColocar(float worldX, float worldY, Movel m) {
        if (!editavel || m == null) {
            return null;
        }
        int[] rc = anchorSob(worldX, worldY);
        if (rc == null) {
            return null;
        }
        int row = rc[0], col = rc[1];
        int w = m.getWidthInTiles();
        int h = m.getHeightInTiles();
        if (row + w > MATRIZ || col + h > MATRIZ) {
            return null; // off the grid
        }
        for (int r = row; r < row + w; r++) {
            for (int c = col; c < col + h; c++) {
                if (ocupacao[r * MATRIZ + c] != null) {
                    return null; // overlaps an existing piece
                }
            }
        }
        ocuparArea(row, col, m);
        if (casa != null) {
            casa.colocar(nomeTile(row, col), m);
        }
        selecionado = -1;
        return nomeTile(row, col);
    }

    /**
     * Re-place {@code m} at a known anchor tile without hit-testing — used to undo
     * a removal (the tile was a valid placement before). Mirrors into the model.
     */
    public void recolocarEm(String tileName, Movel m) {
        int[] rc = parseTile(tileName);
        if (rc == null || m == null) {
            return;
        }
        ocuparArea(rc[0], rc[1], m);
        if (casa != null) {
            casa.colocar(tileName, m);
        }
    }

    /** The currently selected placed móvel, or null. */
    public Movel movelSelecionado() {
        return selecionado < 0 ? null : ocupacao[selecionado];
    }

    /** Anchor tile name ("L{row}C{col}") of the selected móvel, or null. */
    public String tileSelecionadoAncora() {
        Movel m = movelSelecionado();
        if (m == null) {
            return null;
        }
        for (int i = 0; i < ocupacao.length; i++) {
            if (ocupacao[i] == m && anchor[i]) {
                return nomeTile(i / MATRIZ, i % MATRIZ);
            }
        }
        return null;
    }

    /** True if there is a selected tile; fills {@code out} with its world centre. */
    public boolean centroSelecionado(Vector2 out) {
        if (selecionado < 0) {
            return false;
        }
        out.set(centroX[selecionado], centroY[selecionado]);
        return true;
    }

    /** World anchor (centre-x, just below the grid) for the house-name field/label. */
    public void ancoraNome(Vector2 out) {
        out.set(originX, originY - tileH / 2f - 24f);
    }

    private void ocuparArea(int rowAnchor, int colAnchor, Movel m) {
        int w = m.getWidthInTiles();
        int h = m.getHeightInTiles();
        for (int r = rowAnchor; r < rowAnchor + w && r < MATRIZ; r++) {
            for (int c = colAnchor; c < colAnchor + h && c < MATRIZ; c++) {
                int i = r * MATRIZ + c;
                ocupacao[i] = m;
                anchor[i] = (r == rowAnchor && c == colAnchor);
            }
        }
    }

    private int[] anchorSob(float worldX, float worldY) {
        for (int row = MATRIZ - 1; row >= 0; row--) {
            for (int col = MATRIZ - 1; col >= 0; col--) {
                if (poligonos[row * MATRIZ + col].contains(worldX, worldY)) {
                    return new int[]{row, col};
                }
            }
        }
        return null;
    }

    // ---------------------------------------------------------------
    // Geometry + drawing
    // ---------------------------------------------------------------

    /** Recompute the diamond grid (world coords) to fit the actor's bounds. */
    private void recalcular() {
        float areaW = getWidth();
        float areaH = getHeight();
        float baseTileW = 100f, baseTileH = 50f;
        float escala = Math.min(1f, Math.min(areaW / (8f * baseTileW),
                                             areaH / (8f * baseTileH)));
        tileW = baseTileW * escala;
        tileH = baseTileH * escala;
        originX = getX() + areaW / 2f;
        originY = getY() + areaH / 2f - 3.5f * tileH;

        for (int linha = 0; linha < MATRIZ; linha++) {
            for (int coluna = 0; coluna < MATRIZ; coluna++) {
                int i = linha * MATRIZ + coluna;
                float px = originX + (coluna - linha) * (tileW / 2f);
                float py = originY + (coluna + linha) * (tileH / 2f);
                centroX[i] = px;
                centroY[i] = py;
                poligonos[i].setVertices(new float[]{
                        px - tileW / 2f, py,
                        px, py - tileH / 2f,
                        px + tileW / 2f, py,
                        px, py + tileH / 2f
                });
            }
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        recalcular();

        // Soft drop shadow under the whole grid so it lifts off the (dark) sky.
        desenharSombra();

        for (int i = 0; i < poligonos.length; i++) {
            float[] v = poligonos[i].getVertices();
            // Every tile gets a solid fill now (light for empty, muted for
            // occupied) so the grid stays legible against any background.
            drawer.setColor(ocupacao[i] == null ? VAZIO : OCUPADO);
            preencher(v);
            drawer.setColor(CONTORNO);
            contorno(v);
        }

        // Móvel names on their anchor tile (drawn after fills so nothing covers them).
        for (int i = 0; i < ocupacao.length; i++) {
            if (anchor[i] && ocupacao[i] != null) {
                float sx = fonte.getScaleX(), sy = fonte.getScaleY();
                fonte.getData().setScale(0.7f);
                fonte.setColor(Color.WHITE);
                layout.setText(fonte, ocupacao[i].getNome());
                fonte.draw(batch, layout, centroX[i] - layout.width / 2f,
                        centroY[i] + layout.height / 2f);
                fonte.getData().setScale(sx, sy);
                fonte.setColor(Color.BLACK);
            }
        }

        if (editavel && selecionado >= 0) {
            drawer.setColor(SELECAO);
            contorno(poligonos[selecionado].getVertices());
        }
    }

    /** Fill a diamond with the current color (two triangles tessellate it). */
    private void preencher(float[] v) {
        drawer.filledTriangle(v[0], v[1], v[2], v[3], v[4], v[5]);
        drawer.filledTriangle(v[0], v[1], v[4], v[5], v[6], v[7]);
    }

    /**
     * One big translucent diamond matching the 8×8 footprint, offset slightly
     * down/right, drawn before the tiles to read as a soft shadow on the sky.
     */
    private void desenharSombra() {
        float off = Math.max(5f, tileH * 0.18f);
        float topX = originX + off,                 topY = originY - tileH / 2f - off;
        float rightX = originX + 4f * tileW + off,  rightY = originY + 3.5f * tileH - off;
        float botX = originX + off,                 botY = originY + 7.5f * tileH - off;
        float leftX = originX - 4f * tileW + off,   leftY = originY + 3.5f * tileH - off;
        drawer.setColor(Palette.TILE_SOMBRA);
        drawer.filledTriangle(topX, topY, rightX, rightY, botX, botY);
        drawer.filledTriangle(topX, topY, botX, botY, leftX, leftY);
    }

    /** Draw a diamond outline as four lines (avoids ShapeDrawer polygon API quirks). */
    private void contorno(float[] v) {
        drawer.line(v[0], v[1], v[2], v[3]);
        drawer.line(v[2], v[3], v[4], v[5]);
        drawer.line(v[4], v[5], v[6], v[7]);
        drawer.line(v[6], v[7], v[0], v[1]);
    }

    // ---------------------------------------------------------------
    // Tile naming
    // ---------------------------------------------------------------

    private static String nomeTile(int row, int col) {
        return "L" + row + "C" + col;
    }

    private static int[] parseTile(String tile) {
        if (tile == null || !tile.matches("L\\d+C\\d+")) {
            return null;
        }
        int cIdx = tile.indexOf('C');
        try {
            int row = Integer.parseInt(tile.substring(1, cIdx));
            int col = Integer.parseInt(tile.substring(cIdx + 1));
            if (row < 0 || row >= MATRIZ || col < 0 || col >= MATRIZ) {
                return null;
            }
            return new int[]{row, col};
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
