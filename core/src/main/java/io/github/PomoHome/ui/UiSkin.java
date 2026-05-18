package io.github.PomoHome.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

/**
 * Builds a scene2d {@link Skin} entirely in code — no uiskin.json / atlas /
 * .fnt assets needed (we ship none). It uses libGDX's built-in default
 * {@link BitmapFont} and a single 1x1 white {@link Texture} that every
 * drawable tints via {@link Skin#newDrawable(String, Color)}.
 *
 * Disposal: the font and the white texture are registered in the Skin, so
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

        BitmapFont font = new BitmapFont(); // libGDX built-in font.
        skin.add("default-font", font);     // Skin owns + disposes the font.

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

        return skin;
    }

    private static Drawable pad(Drawable d) {
        d.setLeftWidth(14f);
        d.setRightWidth(14f);
        d.setTopHeight(10f);
        d.setBottomHeight(10f);
        return d;
    }
}
