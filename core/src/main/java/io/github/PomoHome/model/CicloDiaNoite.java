package io.github.PomoHome.model;

import com.badlogic.gdx.math.MathUtils;

/**
 * The world's day↔night clock (RNF02). A single instance lives in {@link Jogo};
 * only {@code TelaJogo} advances it (so the world progresses solely while the
 * main house screen is open), while every screen reads {@link #fatorNoite()} to
 * tint its background — the cycle stays in sync everywhere.
 *
 * <p>One full loop is {@link #CICLO_SEGUNDOS} (50 minutes): the first half slides
 * from day into night, the second half back into day. The blend is a smooth
 * cosine so there is no hard flip — at {@code t=0} it is full day, at the
 * half-cycle (25&nbsp;min) full night, and back to full day at the end.
 */
public class CicloDiaNoite {

    /** 50-minute loop: 25 min toward night, 25 min back toward day. */
    public static final float CICLO_SEGUNDOS = 50f * 60f;

    /**
     * Elapsed seconds within the current loop, wrapped to [0, CICLO_SEGUNDOS).
     * Starts a quarter in (midday) so entering the game opens on a bright sky with
     * the sun at its zenith, then progresses toward sunset, night and back.
     */
    private float tempo = CICLO_SEGUNDOS / 4f;

    /** Advance the clock by {@code delta} seconds, wrapping at the cycle length. */
    public void avancar(float delta) {
        if (delta <= 0f) {
            return;
        }
        tempo = (tempo + delta) % CICLO_SEGUNDOS;
    }

    /**
     * How "night" the world is right now, in {@code [0, 1]}: {@code 0} = full day
     * (sun at zenith), {@code 1} = full night (moon at zenith). Smooth so the
     * transition is gradual; aligned with the sun's height so noon is brightest
     * and midnight darkest. Screens lerp their sky color by this factor.
     */
    public float fatorNoite() {
        return 0.5f - 0.5f * MathUtils.sin(MathUtils.PI2 * tempo / CICLO_SEGUNDOS);
    }

    /**
     * Position within the loop in {@code [0, 1)}, used to drive the sun/moon arc:
     * {@code [0, 0.5)} is daytime (sun crossing the sky), {@code [0.5, 1)} night
     * (moon crossing). Sunrise is at {@code 0}, noon {@code 0.25}, sunset
     * {@code 0.5}, midnight {@code 0.75}.
     */
    public float fase() {
        return tempo / CICLO_SEGUNDOS;
    }

    public float getTempo() { return tempo; }
    public void setTempo(float tempo) { this.tempo = tempo % CICLO_SEGUNDOS; }
}
