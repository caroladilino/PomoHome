package io.github.PomoHome.ui.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import io.github.PomoHome.model.Timer;
import space.earlygrey.shapedrawer.ShapeDrawer;

/**
 * The Pomodoro ring as a scene2d {@link Actor}, drawn through a
 * {@link ShapeDrawer} (which renders shapes via the Stage's {@code Batch}, so it
 * coexists with the rest of the scene2d UI — no {@code ShapeRenderer}/{@code
 * Batch} begin-end dance). A light grey disc, a darker sector for the remaining
 * fraction, and a cream inner disc form the ring; the {@code MM:SS} text is drawn
 * centred with the bold timer font.
 *
 * <p>Display-only — it reads the {@link Timer} each frame and isn't interactive
 * (the +/- and start/pause controls are separate buttons).
 */
public class TimerRingActor extends Actor {

    private static final Color ANEL_FUNDO = Color.valueOf("#D3D3D3");
    private static final Color ANEL_PROGRESSO = Color.valueOf("#696969");
    private static final Color MIOLO = Color.valueOf("#EDE8D8");

    private final Timer timer;
    private final ShapeDrawer drawer;
    private final BitmapFont fonte;
    private final GlyphLayout layout = new GlyphLayout();

    private float raioExterno = 110f;
    private float raioInterno = 85f;

    public TimerRingActor(Timer timer, ShapeDrawer drawer, BitmapFont fonte) {
        this.timer = timer;
        this.drawer = drawer;
        this.fonte = fonte;
    }

    /** Set the ring radii; the actor's bounds should be a {@code 2·raioExterno} box. */
    public void setRaios(float raioExterno, float raioInterno) {
        this.raioExterno = raioExterno;
        this.raioInterno = raioInterno;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float cx = getX() + getWidth() / 2f;
        float cy = getY() + getHeight() / 2f;

        // Ring = grey track disc + a dark "elapsed" pie sector + a cream inner disc
        // that carves out the centre. The sector is a triangle fan because
        // ShapeDrawer's thick arc/sector render unreliably (the arc vanishes mid-sweep).
        drawer.setColor(ANEL_FUNDO);
        drawer.filledCircle(cx, cy, raioExterno);

        float decorrido = 1f - timer.proporcaoRestante(); // fills up as time passes
        if (decorrido > 0f) {
            drawer.setColor(ANEL_PROGRESSO);
            float anguloInicial = MathUtils.PI / 2f;          // top of the circle
            float varredura = -(decorrido * MathUtils.PI2);   // clockwise
            int segmentos = Math.max(2, MathUtils.ceil(decorrido * 180f));
            float passo = varredura / segmentos;
            for (int i = 0; i < segmentos; i++) {
                float a0 = anguloInicial + passo * i;
                float a1 = anguloInicial + passo * (i + 1);
                drawer.filledTriangle(
                        cx, cy,
                        cx + raioExterno * MathUtils.cos(a0), cy + raioExterno * MathUtils.sin(a0),
                        cx + raioExterno * MathUtils.cos(a1), cy + raioExterno * MathUtils.sin(a1));
            }
        }

        drawer.setColor(MIOLO);
        drawer.filledCircle(cx, cy, raioInterno);

        int minutos = (int) (timer.getTempoAtual() / 60);
        int segundos = (int) (timer.getTempoAtual() % 60);
        layout.setText(fonte, String.format("%02d:%02d", minutos, segundos));
        fonte.setColor(Color.BLACK);
        fonte.draw(batch, layout, cx - (layout.width / 2f), cy + (layout.height / 2f));
    }
}
