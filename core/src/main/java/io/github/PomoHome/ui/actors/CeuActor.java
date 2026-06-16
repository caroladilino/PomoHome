package io.github.PomoHome.ui.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import io.github.PomoHome.model.CicloDiaNoite;
import io.github.PomoHome.ui.Palette;
import space.earlygrey.shapedrawer.ShapeDrawer;

/**
 * The sky: a sun (by day) or moon (by night) that arcs across the screen as the
 * {@link CicloDiaNoite} advances (RNF02). Drawn through the Stage's
 * {@link ShapeDrawer} like every other shape in the game — no PNG asset needed.
 *
 * <p>It lives behind the rest of the scene (added first), so the disc peeks
 * through the open sky around the panel and house. The body rises in the east,
 * peaks overhead at noon/midnight and sets in the west, following the cycle
 * phase; the moon also gets a crescent carved with the current sky color.
 */
public class CeuActor extends Actor {

    private final CicloDiaNoite ciclo;
    private final ShapeDrawer drawer;

    // Reused glow colors (sun/moon disc + a soft halo behind it).
    private final Color halo = new Color();

    public CeuActor(CicloDiaNoite ciclo, ShapeDrawer drawer) {
        this.ciclo = ciclo;
        this.drawer = drawer;
        setTouchable(Touchable.disabled);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float fase = ciclo.fase();
        float anguloCeu = MathUtils.PI2 * fase;
        float seno = MathUtils.sin(anguloCeu);     // >= 0 by day, < 0 by night
        boolean dia = seno >= 0f;
        float altura = Math.abs(seno);             // 0 at the horizon, 1 overhead

        // Horizontal sweep: left (rise) to right (set) over each half-cycle.
        float progressoX = dia ? fase * 2f : fase * 2f - 1f;

        float margem = getWidth() * 0.08f;
        float cx = getX() + margem + progressoX * (getWidth() - 2f * margem);
        float horizonte = getY() + getHeight() * 0.45f;
        float zenite = getY() + getHeight() * 0.95f;
        float cy = horizonte + altura * (zenite - horizonte);

        float raio = MathUtils.clamp(Math.min(getWidth(), getHeight()) * 0.035f, 22f, 40f);

        Color corpo = dia ? Palette.SOL : Palette.LUA;
        // Soft halo behind the disc.
        halo.set(corpo);
        halo.a = 0.22f;
        drawer.setColor(halo);
        drawer.filledCircle(cx, cy, raio * 1.9f);
        // The disc itself.
        drawer.setColor(corpo);
        drawer.filledCircle(cx, cy, raio);
        // Moon: carve a crescent with the current sky color.
        if (!dia) {
            drawer.setColor(Palette.ceu(ciclo.fatorNoite()));
            drawer.filledCircle(cx + raio * 0.55f, cy + raio * 0.28f, raio * 0.92f);
        }
    }
}
