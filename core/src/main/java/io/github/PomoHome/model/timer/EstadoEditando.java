package io.github.PomoHome.model.timer;

/**
 * Cycle-edit state (old {@code EstadoTimer.EDITANDO}): the player is adjusting
 * the cycle length with the + / − buttons (those mutate the {@link
 * io.github.PomoHome.model.Timer} directly from the screen — they are config
 * tweaks, not state transitions). ACEITAR confirms and returns to idle.
 */
public class EstadoEditando implements EstadoTimer {

    @Override
    public void aceitar(ContextoTimer ctx) {
        ctx.transicionar(new EstadoParado());
    }

    @Override public String textoBotaoEsquerdo() { return "INICIAR"; }
    @Override public String textoBotaoDireito() { return "EDITAR"; }
    @Override public boolean mostraIniciarEditar() { return false; }
    @Override public boolean mostraNavegacao() { return false; }
    @Override public boolean mostraControlesEdicao() { return true; }
}
