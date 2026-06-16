package io.github.PomoHome.model.timer;

/**
 * Paused state (old {@code EstadoTimer.PAUSADO}): a run is in progress but the
 * countdown is held. The left button resumes; the right button cancels (reset).
 */
public class EstadoPausado implements EstadoTimer {

    @Override
    public void iniciarOuPausar(ContextoTimer ctx) {
        ctx.getTimer().iniciarOuPausar(); // toggles rodando -> true
        ctx.transicionar(new EstadoRodando());
    }

    @Override
    public void editarOuCancelar(ContextoTimer ctx) {
        ctx.getTimer().resetar();
        ctx.transicionar(new EstadoParado());
    }

    @Override public String textoBotaoEsquerdo() { return "RETOMAR"; }
    @Override public String textoBotaoDireito() { return "CANCELAR"; }
    @Override public boolean mostraIniciarEditar() { return true; }
    @Override public boolean mostraNavegacao() { return false; }
    @Override public boolean mostraControlesEdicao() { return false; }
}
