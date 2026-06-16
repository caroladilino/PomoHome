package io.github.PomoHome.ui;

import com.badlogic.gdx.graphics.Color;

/**
 * The single source of truth for every color in the client.
 *
 * <p>PomoHome's look is "cozy focus": a warm parchment surface, a terracotta
 * accent that nods to the Pomodoro tomato, a calm sage green for buttons, and a
 * soft gold for highlights — all sitting on a deep indigo background. Pick from
 * here instead of hard-coding hex values so every screen stays in harmony.
 *
 * <p>The {@link Color} constants are shared and must be treated as read-only.
 * Scene2d/ShapeDrawer copy the color when you call {@code setColor} /
 * {@code newDrawable} / {@code Label#setColor}, so sharing a single instance is
 * safe as long as nobody mutates the fields in place.
 *
 * <p>See {@code core/UI_DESIGN.md} for the rationale and usage guide.
 */
public final class Palette {

    private Palette() { }

    // --- Backgrounds ---------------------------------------------------------
    /** Deep indigo behind every screen (the {@code ScreenUtils.clear} color). */
    public static final Color FUNDO = Color.valueOf("#20212B");

    // --- Day/night sky (RNF02) ----------------------------------------------
    /**
     * The screen background tints between these as the world cycles day↔night.
     * Both stay dark enough that the white UI text remains legible — "day" is a
     * dusky slate-blue, "night" a deep indigo. {@link #ceu(float)} blends them.
     */
    public static final Color CEU_DIA = Color.valueOf("#46537A");
    public static final Color CEU_NOITE = Color.valueOf("#181A26");
    /** The sun and moon discs that arc across the sky as the cycle runs. */
    public static final Color SOL = Color.valueOf("#FFD27A");
    public static final Color LUA = Color.valueOf("#E6E9F2");
    /** Reused by {@link #ceu(float)} — single-threaded render use only. */
    private static final Color CEU = new Color();

    /**
     * Sky color for the current cycle phase: {@code fatorNoite} 0 → day, 1 →
     * night, blended linearly. Returns a shared instance overwritten on each
     * call, so use it immediately (e.g. pass straight to {@code ScreenUtils.clear})
     * and don't retain it.
     */
    public static Color ceu(float fatorNoite) {
        return CEU.set(CEU_DIA).lerp(CEU_NOITE, fatorNoite);
    }

    // --- Surfaces ------------------------------------------------------------
    /** Warm parchment: the game panel and the timer's inner disc. */
    public static final Color PERGAMINHO = Color.valueOf("#F2EAD8");
    /** A darker parchment used to outline/inset the panel (replaces the old pink). */
    public static final Color PERGAMINHO_BORDA = Color.valueOf("#D8CBB0");
    /** Tinted cells in the store grid. */
    public static final Color CELULA_LOJA = Color.valueOf("#CDBFA6");
    /** Tinted cells in the inventory grid. */
    public static final Color CELULA_INV = Color.valueOf("#D6CDBC");

    // --- Accents -------------------------------------------------------------
    /** Terracotta — the Pomodoro energy: timer progress, primary emphasis. */
    public static final Color TERRACOTA = Color.valueOf("#E07A5F");
    public static final Color TERRACOTA_PRESS = Color.valueOf("#C75D43");
    /** Sage green — calm, used for all buttons. */
    public static final Color SAGE = Color.valueOf("#81B29A");
    public static final Color SAGE_PRESS = Color.valueOf("#6A9A82");
    public static final Color SAGE_HOVER = Color.valueOf("#74A88D");
    /** Soft gold — coins, selection highlight, "this is me" in the ranking. */
    public static final Color OURO = Color.valueOf("#E9C46A");
    /** A deeper gold that reads as text on the cream panel. */
    public static final Color OURO_TEXTO = Color.valueOf("#B07D2B");

    // --- Text + status -------------------------------------------------------
    /** Dark indigo text, for use on the parchment surfaces. */
    public static final Color TEXTO_ESCURO = Color.valueOf("#2B2D42");
    /** Parchment-white text, for use on the dark background. */
    public static final Color TEXTO_CLARO = Color.valueOf("#F2EAD8");
    public static final Color ERRO = Color.valueOf("#E06C75");
    public static final Color SUCESSO = Color.valueOf("#81B29A");
    public static final Color NEUTRO = Color.valueOf("#B8B8BE");

    // --- Disabled ------------------------------------------------------------
    public static final Color DESABILITADO = Color.valueOf("#5A5B64");
    public static final Color DESABILITADO_TEXTO = Color.valueOf("#9A9AA2");

    // --- House grid ----------------------------------------------------------
    /** Empty floor tile: a light fill so the grid reads clearly on the dark sky. */
    public static final Color TILE_VAZIO = Color.valueOf("#D7DEEA");
    public static final Color TILE_OCUPADO = Color.valueOf("#8B9BB4");
    public static final Color TILE_CONTORNO = Color.valueOf("#2B2D42");
    public static final Color TILE_SELECAO = OURO;
    /** Soft drop shadow cast under the grid to lift it off the background. */
    public static final Color TILE_SOMBRA = new Color(0f, 0f, 0f, 0.28f);

    // --- Timer ring ----------------------------------------------------------
    public static final Color ANEL_FUNDO = Color.valueOf("#C9B79C");
    public static final Color ANEL_PROGRESSO = TERRACOTA;
    public static final Color ANEL_MIOLO = PERGAMINHO;
    /**
     * Timer-phase colors (RNF03): the progress sector starts blue when the cycle
     * begins and shifts gradually to pink as it nears completion. The actor
     * interpolates between these two each frame — treat them as read-only.
     */
    public static final Color TIMER_INICIO = Color.valueOf("#4D9DE0"); // azul (cycle start)
    public static final Color TIMER_FIM = Color.valueOf("#E07A9F");    // rosa (cycle end)

    // --- Field chrome (text inputs) -----------------------------------------
    public static final Color CAMPO_FUNDO = new Color(1f, 1f, 1f, 0.10f);
    public static final Color CAMPO_SELECAO = new Color(0.51f, 0.70f, 0.60f, 0.40f); // sage @ 40%
}
