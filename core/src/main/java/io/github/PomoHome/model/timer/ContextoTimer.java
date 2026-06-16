package io.github.PomoHome.model.timer;

import io.github.PomoHome.model.Timer;

import java.util.ArrayList;
import java.util.List;

/**
 * Context for the Pomodoro {@link EstadoTimer} state machine (GoF State) and
 * subject for {@link OuvinteSessao} observers (GoF Observer).
 *
 * <p>It composes the existing {@link Timer} model (countdown data + helpers,
 * unchanged) and the current state object. The screen delegates the timer
 * buttons / the render tick here; the state objects drive transitions via
 * {@link #transicionar} and fire cycle completion via {@link #notificarConclusao}.
 */
public class ContextoTimer {

    private final Timer timer;
    private EstadoTimer estado;
    private final List<OuvinteSessao> ouvintes = new ArrayList<>();

    public ContextoTimer() {
        this(new Timer());
    }

    public ContextoTimer(Timer timer) {
        this.timer = timer;
        this.estado = new EstadoParado();
    }

    // --- State machine ---

    public Timer getTimer() { return timer; }

    public EstadoTimer getEstado() { return estado; }

    /** Called by the states to move to the next one. */
    public void transicionar(EstadoTimer novo) { this.estado = novo; }

    public void iniciarOuPausar() { estado.iniciarOuPausar(this); }

    public void editarOuCancelar() { estado.editarOuCancelar(this); }

    public void aceitar() { estado.aceitar(this); }

    public void atualizar(float delta) { estado.atualizar(this, delta); }

    // --- Observer (subject) ---

    public void adicionarOuvinte(OuvinteSessao ouvinte) {
        if (ouvinte != null && !ouvintes.contains(ouvinte)) {
            ouvintes.add(ouvinte);
        }
    }

    public void removerOuvinte(OuvinteSessao ouvinte) {
        ouvintes.remove(ouvinte);
    }

    /** Notify every observer that a cycle just completed. Called by the states. */
    public void notificarConclusao(int minutos) {
        for (OuvinteSessao ouvinte : ouvintes) {
            ouvinte.cicloConcluido(minutos);
        }
    }
}
