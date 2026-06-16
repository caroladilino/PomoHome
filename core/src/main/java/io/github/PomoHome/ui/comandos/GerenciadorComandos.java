package io.github.PomoHome.ui.comandos;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Invoker for {@link ComandoEdicao} (GoF Command): runs commands and keeps undo
 * / redo stacks. A fresh command clears the redo stack (the usual editor rule).
 * Scoped to one house-editing session — {@link #limpar()} resets it when edit
 * mode opens.
 */
public class GerenciadorComandos {

    private final Deque<ComandoEdicao> pilhaDesfazer = new ArrayDeque<>();
    private final Deque<ComandoEdicao> pilhaRefazer = new ArrayDeque<>();

    /** Run a command; on success record it for undo and drop the redo history. */
    public boolean executar(ComandoEdicao comando) {
        if (comando == null || !comando.executar()) {
            return false;
        }
        pilhaDesfazer.push(comando);
        pilhaRefazer.clear();
        return true;
    }

    public void desfazer() {
        if (pilhaDesfazer.isEmpty()) {
            return;
        }
        ComandoEdicao comando = pilhaDesfazer.pop();
        comando.desfazer();
        pilhaRefazer.push(comando);
    }

    public void refazer() {
        if (pilhaRefazer.isEmpty()) {
            return;
        }
        ComandoEdicao comando = pilhaRefazer.pop();
        comando.executar();
        pilhaDesfazer.push(comando);
    }

    public boolean podeDesfazer() { return !pilhaDesfazer.isEmpty(); }

    public boolean podeRefazer() { return !pilhaRefazer.isEmpty(); }

    public void limpar() {
        pilhaDesfazer.clear();
        pilhaRefazer.clear();
    }
}
