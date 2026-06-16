package io.github.PomoHome.ui.comandos;

import io.github.PomoHome.model.Movel;
import io.github.PomoHome.ui.actors.CasaActor;

/**
 * Removes the currently selected móvel from the grid (back to the inventory).
 * The móvel and its anchor tile are captured at construction so undo can put it
 * back exactly where it was.
 */
public class ComandoRemover implements ComandoEdicao {

    private final CasaActor casaActor;
    private final Movel movel;
    private final String tileAncora;

    public ComandoRemover(CasaActor casaActor) {
        this.casaActor = casaActor;
        this.movel = casaActor.movelSelecionado();
        this.tileAncora = casaActor.tileSelecionadoAncora();
    }

    @Override
    public boolean executar() {
        if (movel == null || tileAncora == null) {
            return false;
        }
        return casaActor.removerInstancia(movel);
    }

    @Override
    public void desfazer() {
        casaActor.recolocarEm(tileAncora, movel);
    }
}
