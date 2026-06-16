package io.github.PomoHome.ui.comandos;

import io.github.PomoHome.model.Movel;
import io.github.PomoHome.ui.actors.CasaActor;

/**
 * Places a móvel on the grid at the dropped position. Undo removes that same
 * instance again; the inventory re-derives it automatically.
 */
public class ComandoColocar implements ComandoEdicao {

    private final CasaActor casaActor;
    private final Movel movel;
    private final float worldX;
    private final float worldY;

    public ComandoColocar(CasaActor casaActor, Movel movel, float worldX, float worldY) {
        this.casaActor = casaActor;
        this.movel = movel;
        this.worldX = worldX;
        this.worldY = worldY;
    }

    @Override
    public boolean executar() {
        // tentarColocar returns the anchor tile on success, null if it didn't fit.
        return casaActor.tentarColocar(worldX, worldY, movel) != null;
    }

    @Override
    public void desfazer() {
        casaActor.removerInstancia(movel);
    }
}
