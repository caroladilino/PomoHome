package io.github.PomoHome;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class InventarioView {
    public boolean expandido = false;
    private float painelWidth = 250f;
    private float x, y, width, height;
    private float abaWidth = 40f;
    private float abaHeight = 80f;
    private float abaX, abaY;

    public void calcularLayout(float telaWidth, float telaHeight) {
        this.width = painelWidth;
        this.height = telaHeight;
        this.y = 0;
        this.x = expandido ? telaWidth - this.width : telaWidth;
        this.abaX = this.x - abaWidth;
        this.abaY = (telaHeight / 2f) - (abaHeight / 2f); 
    }

    public void atualizarLogica(float mouseX, float mouseY, boolean clicou) {
        if (clicou && mouseX >= abaX && mouseX <= abaX + abaWidth && mouseY >= abaY && mouseY <= abaY + abaHeight) {
            expandido = !expandido; 
        }
    }

    public boolean isClicadoNoPainel(float mouseX, float mouseY) {
        if (!expandido) return false;
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    // Calcula se clicou num móvel da lista do jogador
    public Movel getMovelClicado(float mouseX, float mouseY, Jogador jogador) {
        if (!expandido) return null;
        float startX = x + 25f;
        float startY = height - 100f;
        float size = 60f;
        float espaco = 20f;
        int colunas = 2;

        for (int i = 0; i < jogador.inventario.size(); i++) {
            float posX = startX + (i % colunas) * (size + espaco);
            float posY = startY - (i / colunas) * (size + espaco + 30f);

            if (mouseX >= posX && mouseX <= posX + size && mouseY >= posY && mouseY <= posY + size) {
                return jogador.inventario.get(i);
            }
        }
        return null;
    }

    public void desenhar(ShapeRenderer sr, SpriteBatch batch, BitmapFont fonte, Jogador jogador) {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(Color.valueOf("#B0B59E")); 
        sr.rect(abaX, abaY, abaWidth, abaHeight);

        if (expandido) {
            sr.setColor(Color.valueOf("#EAECD7")); 
            sr.rect(x, y, width, height);

            float startX = x + 25f;
            float startY = height - 100f;
            float size = 60f; 
            float espaco = 20f;
            
            for (int i = 0; i < jogador.inventario.size(); i++) {
                float posX = startX + (i % 2) * (size + espaco);
                float posY = startY - (i / 2) * (size + espaco + 30f);

                sr.setColor(Color.LIGHT_GRAY);
                sr.rect(posX, posY, size, size);
            }
        }
        sr.end();

        batch.begin();
        fonte.setColor(Color.BLACK);
        fonte.draw(batch, expandido ? ">" : "<", abaX + 12, abaY + (abaHeight/2) + 5);

        if (expandido) {
            fonte.draw(batch, "Inventario", x + 20, height - 30);
            
            for (int i = 0; i < jogador.inventario.size(); i++) {
                Movel m = jogador.inventario.get(i);
                float posX = x + 25f + (i % 2) * (60f + 20f);
                float posY = height - 100f - (i / 2) * (60f + 20f + 30f);
                
                String nomeCurto = m.nome.split(" ")[0]; 
                fonte.getData().setScale(0.8f); 
                fonte.draw(batch, nomeCurto, posX, posY - 5);
                fonte.getData().setScale(1.2f); 
            }
        }
        batch.end();
    }
}