package io.github.PomoHome.model.timer;

import io.github.PomoHome.model.Timer;

/**
 * Running state (old {@code EstadoTimer.RODANDO}): the countdown is ticking.
 * The left button pauses; the right button cancels (reset). The per-frame tick
 * is the only place a cycle can complete — when it does, this state hands back
 * to {@link EstadoParado} and notifies the observers (GoF Observer).
 */
public class EstadoRodando implements EstadoTimer {

    @Override
    public void iniciarOuPausar(ContextoTimer ctx) {
        ctx.getTimer().iniciarOuPausar(); // toggles rodando -> false
        ctx.transicionar(new EstadoPausado());
    }

    @Override
    public void editarOuCancelar(ContextoTimer ctx) {
        ctx.getTimer().resetar();
        ctx.transicionar(new EstadoParado());
    }

    @Override
    public void atualizar(ContextoTimer ctx, float delta) {
        Timer timer = ctx.getTimer();
        if (timer.atualizar(delta)) { // true exactly on the frame it hits zero
            int minutos = timer.minutosDoCiclo();
            ctx.transicionar(new EstadoParado());
            ctx.notificarConclusao(minutos);
        }
    }

    @Override public String textoBotaoEsquerdo() { return "PAUSAR"; }
    @Override public String textoBotaoDireito() { return "CANCELAR"; }
    @Override public boolean mostraIniciarEditar() { return true; }
    @Override public boolean mostraNavegacao() { return false; }
    @Override public boolean mostraControlesEdicao() { return false; }
}
