package io.github.PomoHome.ui.actors;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import io.github.PomoHome.ui.Palette;
import space.earlygrey.shapedrawer.ShapeDrawer;

/**
 * The game screen's left panel: a rounded parchment surface with a soft taupe
 * outline, drawn through the Stage's batch via {@link ShapeDrawer}.
 * Non-interactive — the buttons / store / inventory are separate actors layered
 * on top.
 *
 * <p>{@link #BORDA} is the outline thickness; the screen reuses it to inset the
 * panel's content (scroll panes, labels). The old loud pink border was replaced
 * by {@link Palette#PERGAMINHO_BORDA}, a darker shade of the parchment that
 * defines the edge without competing with the content.
 */
public class PainelActor extends Actor {

    public static final float BORDA = 28f;
    private static final float RAIO = BORDA * 2f;

    private final ShapeDrawer drawer;

    public PainelActor(ShapeDrawer drawer) {
        this.drawer = drawer;
        setTouchable(Touchable.disabled);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float x = getX(), y = getY(), w = getWidth(), h = getHeight();
        drawer.setColor(Palette.PERGAMINHO_BORDA);
        arredondado(x, y, w, h, RAIO);
        drawer.setColor(Palette.PERGAMINHO);
        arredondado(x + BORDA, y + BORDA, w - 2f * BORDA, h - 2f * BORDA,
                Math.max(0f, RAIO - BORDA));
    }

    /** Filled rounded rectangle: two overlapping rects plus four corner discs. */
    private void arredondado(float x, float y, float w, float h, float r) {
        drawer.filledRectangle(x + r, y, w - 2f * r, h);
        drawer.filledRectangle(x, y + r, w, h - 2f * r);
        drawer.filledCircle(x + r, y + r, r);
        drawer.filledCircle(x + w - r, y + r, r);
        drawer.filledCircle(x + r, y + h - r, r);
        drawer.filledCircle(x + w - r, y + h - r, r);
    }
}
