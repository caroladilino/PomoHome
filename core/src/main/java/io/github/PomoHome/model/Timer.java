package io.github.PomoHome.model;

/**
 * Pomodoro countdown timer (ported from the gameplay prototype).
 *
 * <ul>
 *   <li>{@code tempoCiclo}  — the configured cycle length, in <b>seconds</b>.</li>
 *   <li>{@code tempoAtual}  — time left, as a float so we can subtract the
 *       engine's per-frame delta precisely.</li>
 *   <li>{@code rodando}     — whether the countdown is currently ticking.</li>
 * </ul>
 *
 * The screen calls {@link #atualizar(float)} every frame; it returns
 * {@code true} on the single frame the cycle reaches zero, which is the cue
 * to POST the completed session to the backend ({@code registrarSessao}).
 */
public class Timer {

    /** Editing bounds: 5 to 95 minutes, in 5-minute steps (matches the prototype). */
    public static final int PASSO_SEGUNDOS = 5 * 60;
    public static final int MIN_SEGUNDOS = 5 * 60;
    public static final int MAX_SEGUNDOS = 95 * 60;
    public static final int PADRAO_SEGUNDOS = 25 * 60;

    private int tempoCiclo;
    private float tempoAtual;
    private boolean rodando;

    public Timer() {
        this(PADRAO_SEGUNDOS);
    }

    public Timer(int tempoCicloSegundos) {
        this.tempoCiclo = tempoCicloSegundos;
        this.tempoAtual = tempoCicloSegundos;
        this.rodando = false;
    }

    /**
     * Advance the countdown by {@code delta} seconds.
     * @return true exactly once, on the frame the cycle hits zero.
     */
    public boolean atualizar(float delta) {
        if (rodando && tempoAtual > 0) {
            tempoAtual -= delta;
            if (tempoAtual <= 0) {
                tempoAtual = 0;
                rodando = false;
                return true; // Pomodoro completed!
            }
        }
        return false;
    }

    public void iniciarOuPausar() {
        rodando = !rodando;
    }

    public void resetar() {
        tempoAtual = tempoCiclo;
        rodando = false;
    }

    /** Increase the cycle by one step (clamped), resetting the countdown. */
    public void aumentarCiclo() {
        if (tempoCiclo + PASSO_SEGUNDOS <= MAX_SEGUNDOS) {
            tempoCiclo += PASSO_SEGUNDOS;
            resetar();
        }
    }

    /** Decrease the cycle by one step (clamped), resetting the countdown. */
    public void diminuirCiclo() {
        if (tempoCiclo - PASSO_SEGUNDOS >= MIN_SEGUNDOS) {
            tempoCiclo -= PASSO_SEGUNDOS;
            resetar();
        }
    }

    /** Coins earned on completion: one per configured minute. */
    public int minutosDoCiclo() {
        return tempoCiclo / 60;
    }

    /** 0..1 fraction of the cycle still remaining (drives the ring arc). */
    public float proporcaoRestante() {
        return tempoCiclo == 0 ? 0f : tempoAtual / tempoCiclo;
    }

    public int getTempoCiclo() { return tempoCiclo; }
    public void setTempoCiclo(int tempoCiclo) { this.tempoCiclo = tempoCiclo; }

    public float getTempoAtual() { return tempoAtual; }
    public void setTempoAtual(float tempoAtual) { this.tempoAtual = tempoAtual; }

    public boolean isRodando() { return rodando; }
    public void setRodando(boolean rodando) { this.rodando = rodando; }
}
