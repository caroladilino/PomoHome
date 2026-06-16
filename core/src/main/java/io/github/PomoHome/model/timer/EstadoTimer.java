package io.github.PomoHome.model.timer;

/**
 * A state of the Pomodoro timer (GoF State).
 *
 * <p>Each concrete state owns both its transitions (what the buttons / the
 * per-frame tick do while in this state) and the UI rules that used to be a
 * forest of {@code if (estadoAtual == …)} checks in {@code TelaJogo}. The
 * screen now delegates the four actions to {@link ContextoTimer} and reads the
 * query methods to lay out its buttons — adding a new state no longer means
 * editing every method in the screen.
 *
 * <p>Actions default to no-ops so each state only overrides the transitions it
 * actually allows.
 */
public interface EstadoTimer {

    // --- Actions (triggered by the timer buttons / the render loop) ---

    /** Left button: INICIAR / PAUSAR / RETOMAR. */
    default void iniciarOuPausar(ContextoTimer ctx) { }

    /** Right button: EDITAR (open cycle config) / CANCELAR (reset a run). */
    default void editarOuCancelar(ContextoTimer ctx) { }

    /** ACEITAR button: confirm the edited cycle length and leave edit mode. */
    default void aceitar(ContextoTimer ctx) { }

    /** Per-frame tick. Only the running state advances the countdown. */
    default void atualizar(ContextoTimer ctx, float delta) { }

    // --- UI queries (replace the old enum comparisons in atualizarBotoes) ---

    String textoBotaoEsquerdo();

    String textoBotaoDireito();

    /** Whether the left/right (INICIAR/EDITAR …) buttons are shown. */
    boolean mostraIniciarEditar();

    /** Whether the navigation buttons (LOJA/EDITAR CASA/RANKING/…) are shown. */
    boolean mostraNavegacao();

    /** Whether the cycle-edit controls (ACEITAR / + / −) are shown. */
    boolean mostraControlesEdicao();
}
