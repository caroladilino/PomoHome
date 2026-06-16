package io.github.PomoHome.ui.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.PomoHome.model.Movel;
import io.github.PomoHome.ui.Palette;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.util.function.Supplier;

/**
 * Draws the inventory item "in hand" as a square following the cursor (only while
 * one is held). Topmost overlay actor; non-interactive.
 */
public class CursorMovelActor extends Actor {

    private static final Color COR = Palette.TILE_OCUPADO;
    private static final float LADO = 60f;

    private final Supplier<Movel> naMao;
    private final ShapeDrawer drawer;
    private final Viewport viewport;
    private final Vector2 tmp = new Vector2();

    public CursorMovelActor(Supplier<Movel> naMao, ShapeDrawer drawer, Viewport viewport) {
        this.naMao = naMao;
        this.drawer = drawer;
        this.viewport = viewport;
        setTouchable(Touchable.disabled);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (naMao.get() == null) {
            return;
        }
        viewport.unproject(tmp.set(Gdx.input.getX(), Gdx.input.getY()));
        drawer.setColor(COR);
        drawer.filledRectangle(tmp.x - LADO / 2f, tmp.y - LADO / 2f, LADO, LADO);
    }
}
