package io.github.PomoHome;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Assets {
    private final AssetManager manager;
    private TextureRegion houseFull;
    private TextureRegion table;

    public Assets() {
        manager = new AssetManager();
        
        // Carrega as texturas. No seu projeto real, você terá os arquivos físicos.
        // Vou assumir que você tem os arquivos cabana_completa.png e mesa.png.
        // Para o exemplo, vou 'simular' o carregamento.
        manager.load("cabin.png", Texture.class);
        manager.load("mesa.png", Texture.class); // Asset da caixa isométrico
    }

    public void update() {
        manager.update();
    }

    public boolean isLoaded() {
        return manager.isFinished();
    }

    public void finishLoading() {
        manager.finishLoading();
        
        // Cabana Completa (Canto inferior direito recortado de image_4.png)
        houseFull = new TextureRegion(manager.get("cabana_completa.png", Texture.class));
        
        // Mesa (Asset isométrico de image_2.png)
        table = new TextureRegion(manager.get("mesa.png", Texture.class));
    }

    public TextureRegion getHouseFull() { return houseFull; }
    public TextureRegion getTable() { return table; }

    public void dispose() {
        manager.dispose();
    }
}