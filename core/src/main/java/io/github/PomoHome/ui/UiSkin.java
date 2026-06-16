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

    // All colors come from the central Palette so every screen stays in harmony.

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
        tf.cursor = skin.newDrawable("white", Color.WHITE);
        tf.selection = skin.newDrawable("white", Palette.CAMPO_SELECAO);
        Drawable fieldBg = skin.newDrawable("white", Palette.CAMPO_FUNDO);
        fieldBg.setLeftWidth(8f);
        fieldBg.setRightWidth(8f);
        fieldBg.setTopHeight(6f);
        fieldBg.setBottomHeight(6f);
        tf.background = fieldBg;
        skin.add("default", tf);

        // --- TextButton (default) --- calm sage green with dark parchment text;
        // the single button look shared across every screen. Reads equally well on
        // the dark background (login/ranking/amigos) and the cream panel.
        TextButton.TextButtonStyle tb = new TextButton.TextButtonStyle();
        tb.font = font;
        tb.up = pad(skin.newDrawable("white", Palette.SAGE));
        tb.down = pad(skin.newDrawable("white", Palette.SAGE_PRESS));
        tb.over = pad(skin.newDrawable("white", Palette.SAGE_HOVER));
        tb.disabled = pad(skin.newDrawable("white", Palette.DESABILITADO));
        tb.fontColor = Palette.TEXTO_ESCURO;
        tb.disabledFontColor = Palette.DESABILITADO_TEXTO;
        skin.add("default", tb);

        // --- TextButton "rosa" --- kept as a style key for backward compatibility
        // (the game menu + TelaVisita ask for it), now the same harmonized sage as
        // the default. The name is historical; there is no longer any pink in the UI.
        skin.add("rosa", tb);

        // --- ScrollPane ---
        // Two styles: "default" for the dark screens (translucent light bars) and
        // "painel" for the parchment game panel (taupe track + sage knob, which
        // reads against the cream). Both give the bar a real width — a bare 1×1
        // drawable renders a 1px sliver you can't see; the knob being shorter than
        // the track is what signals "there's more furniture below".
        skin.add("default", barraRolagem(skin,
                new Color(1f, 1f, 1f, 0.10f), new Color(1f, 1f, 1f, 0.38f)));
        skin.add("painel", barraRolagem(skin, Palette.PERGAMINHO_BORDA, Palette.SAGE_PRESS));

        return skin;
    }

    private static final float LARGURA_BARRA = 8f;

    /** A {@link ScrollPane.ScrollPaneStyle} with a visible, fixed-width scrollbar. */
    private static ScrollPane.ScrollPaneStyle barraRolagem(Skin skin, Color trilho, Color botao) {
        ScrollPane.ScrollPaneStyle sp = new ScrollPane.ScrollPaneStyle();
        sp.vScroll = barra(skin, trilho);
        sp.vScrollKnob = barra(skin, botao);
        sp.hScroll = barra(skin, trilho);
        sp.hScrollKnob = barra(skin, botao);
        return sp;
    }

    /** A 1×1 tinted drawable forced to {@link #LARGURA_BARRA} so the bar is visible. */
    private static Drawable barra(Skin skin, Color cor) {
        Drawable d = skin.newDrawable("white", cor);
        d.setMinWidth(LARGURA_BARRA);
        d.setMinHeight(LARGURA_BARRA);
        return d;
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
