package io.github.PomoHome.ui.comandos;

/**
 * A reversible house-editor action (GoF Command). Each command knows how to do
 * its mutation and how to undo it, so the editor can offer undo/redo without the
 * screen tracking what changed.
 */
public interface ComandoEdicao {

    /** Perform the action. Returns false if it could not be applied (no-op). */
    boolean executar();

    /** Reverse the effect of a successful {@link #executar()}. */
    void desfazer();
}
