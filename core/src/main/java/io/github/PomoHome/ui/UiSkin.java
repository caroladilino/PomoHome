package io.github.PomoHome.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

/**
 * Builds a scene2d {@link Skin} entirely in code — no uiskin.json / atlas /
 * .fnt assets needed. Text is rendered from bundled DejaVuSans .ttf files via
 * FreeType (a crisp body font {@code "default-font"} and a large timer font
 * {@code "timer"}); every drawable tints a single 1x1 white {@link Texture}
 * via {@link Skin#newDrawable(String, Color)}.
 *
 * Disposal: both fonts and the white texture are registered in the Skin, so
 * a single {@code skin.dispose()} frees everything. Build ONE skin in the
 * Game and share it across screens.
 */
public final class UiSkin {

    private UiSkin() { }

    // Palette — kept here so every screen looks consistent.
    private static final Color BG_FIELD   = new Color(1f, 1f, 1f, 0.10f);
    private static final Color CURSOR      = Color.WHITE;
    private static final Color SELECTION   = new Color(0.30f, 0.55f, 0.95f, 0.40f);
    private static final Color BTN_UP      = new Color(0.20f, 0.45f, 0.85f, 1f);
    private static final Color BTN_DOWN    = new Color(0.14f, 0.32f, 0.62f, 1f);
    private static final Color BTN_DISABLED = new Color(0.35f, 0.35f, 0.40f, 1f);

    public static Skin create() {
        Skin skin = new Skin();

        // One 1x1 white pixel; tinted per drawable below.
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        skin.add("white", new Texture(pm)); // Skin owns + disposes the Texture.
        pm.dispose();

        // Crisp text rendered from a bundled .ttf via FreeType, instead of
        // scaling the low-res built-in BitmapFont. DejaVuSans also covers the
        // accented Portuguese glyphs (ã, ç, í, …). Two sizes: a body font for
        // labels/buttons and a large one for the Pomodoro timer digits.
        BitmapFont font = gerarFonte("DejaVuSans.ttf", 18, Color.WHITE);
        // 48px keeps "MM:SS" inside the ring's inner circle (radius ~85) without
        // overflowing onto the grey ring band.
        BitmapFont timerFont = gerarFonte("DejaVuSans-Bold.ttf", 48, Color.BLACK);
        skin.add("default-font", font);  // Skin owns + disposes the font.
        skin.add("timer", timerFont);    // big digits for TelaJogo's ring.

        // --- Label ---
        skin.add("default", new Label.LabelStyle(font, Color.WHITE));

        // --- TextField ---
        TextField.TextFieldStyle tf = new TextField.TextFieldStyle();
        tf.font = font;
        tf.fontColor = Color.WHITE;
        tf.messageFontColor = new Color(1f, 1f, 1f, 0.45f);
        tf.cursor = skin.newDrawable("white", CURSOR);
        tf.selection = skin.newDrawable("white", SELECTION);
        Drawable fieldBg = skin.newDrawable("white", BG_FIELD);
        fieldBg.setLeftWidth(8f);
        fieldBg.setRightWidth(8f);
        fieldBg.setTopHeight(6f);
        fieldBg.setBottomHeight(6f);
        tf.background = fieldBg;
        skin.add("default", tf);

        // --- TextButton ---
        TextButton.TextButtonStyle tb = new TextButton.TextButtonStyle();
        tb.font = font;
        tb.up = pad(skin.newDrawable("white", BTN_UP));
        tb.down = pad(skin.newDrawable("white", BTN_DOWN));
        tb.over = pad(skin.newDrawable("white", BTN_DOWN));
        tb.disabled = pad(skin.newDrawable("white", BTN_DISABLED));
        tb.fontColor = Color.WHITE;
        tb.disabledFontColor = new Color(0.75f, 0.75f, 0.75f, 1f);
        skin.add("default", tb);

        // --- TextButton "rosa" (pink) --- the main game menu (TelaJogo) keeps the
        // prototype's pink buttons with dark text, distinct from the blue default.
        TextButton.TextButtonStyle tbRosa = new TextButton.TextButtonStyle();
        tbRosa.font = font;
        tbRosa.up = pad(skin.newDrawable("white", Color.valueOf("#E58F8F")));
        tbRosa.down = pad(skin.newDrawable("white", Color.valueOf("#C97A7A")));
        tbRosa.over = pad(skin.newDrawable("white", Color.valueOf("#D88585")));
        tbRosa.disabled = pad(skin.newDrawable("white", BTN_DISABLED));
        tbRosa.fontColor = Color.BLACK;
        tbRosa.disabledFontColor = new Color(0.75f, 0.75f, 0.75f, 1f);
        skin.add("rosa", tbRosa);

        // --- ScrollPane (needed by TelaRanking / TelaAmigos lists) ---
        // Without a registered "default" ScrollPaneStyle, `new ScrollPane(actor,
        // skin)` throws "No ScrollPaneStyle registered with name: default".
        ScrollPane.ScrollPaneStyle sp = new ScrollPane.ScrollPaneStyle();
        sp.vScroll = skin.newDrawable("white", new Color(1f, 1f, 1f, 0.06f));
        sp.vScrollKnob = skin.newDrawable("white", new Color(1f, 1f, 1f, 0.25f));
        sp.hScroll = skin.newDrawable("white", new Color(1f, 1f, 1f, 0.06f));
        sp.hScrollKnob = skin.newDrawable("white", new Color(1f, 1f, 1f, 0.25f));
        skin.add("default", sp);

        return skin;
    }

    /**
     * Render a {@link BitmapFont} from a bundled .ttf at {@code size} px using
     * FreeType. Linear filtering keeps it smooth when the viewport scales it.
     * The generator is disposed immediately; the returned font is owned by the
     * caller (registered in the Skin, which disposes it).
     */
    private static BitmapFont gerarFonte(String arquivo, int size, Color cor) {
        FreeTypeFontGenerator generator =
                new FreeTypeFontGenerator(Gdx.files.internal(arquivo));
        FreeTypeFontParameter param = new FreeTypeFontParameter();
        param.size = size;
        param.color = cor;
        param.minFilter = Texture.TextureFilter.Linear;
        param.magFilter = Texture.TextureFilter.Linear;
        BitmapFont font = generator.generateFont(param);
        generator.dispose();
        return font;
    }

    private static Drawable pad(Drawable d) {
        d.setLeftWidth(14f);
        d.setRightWidth(14f);
        d.setTopHeight(10f);
        d.setBottomHeight(10f);
        return d;
    }
}
