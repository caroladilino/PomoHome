package io.github.PomoHome.model.timer;

import io.github.PomoHome.model.Timer;

/**
 * Idle state (old {@code EstadoTimer.PADRAO}): the timer is ready but not
 * counting. From here the player can start a cycle or open the cycle editor,
 * and the navigation buttons are available.
 */
public class EstadoParado implements EstadoTimer {

    @Override
    public void iniciarOuPausar(ContextoTimer ctx) {
        ctx.getTimer().setRodando(true);
        ctx.transicionar(new EstadoRodando());
    }

    @Override
    public void editarOuCancelar(ContextoTimer ctx) {
        ctx.transicionar(new EstadoEditando());
    }

    @Override
    public void atualizar(ContextoTimer ctx, float delta) {
        // After a completed cycle the timer rests at zero; refill it so the ring
        // shows the full cycle again, ready for the next run.
        Timer timer = ctx.getTimer();
        if (timer.getTempoAtual() <= 0) {
            timer.resetar();
        }
    }

    @Override public String textoBotaoEsquerdo() { return "INICIAR"; }
    @Override public String textoBotaoDireito() { return "EDITAR"; }
    @Override public boolean mostraIniciarEditar() { return true; }
    @Override public boolean mostraNavegacao() { return true; }
    @Override public boolean mostraControlesEdicao() { return false; }
}
