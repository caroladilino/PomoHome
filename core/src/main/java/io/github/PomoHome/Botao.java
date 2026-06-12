package io.github.PomoHome;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Botao {
    public String texto;
    public float x, y, width, height, raio;
    public Color corFundo;

    public Botao(String texto, float width, float height, Color corFundo) {
        this.texto = texto;
        this.width = width;
        this.height = height;
        this.corFundo = corFundo;
        this.raio = 10f; // Raio padrão de arredondamento
    }

    public void setPosicao(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public boolean isClicado(float mouseX, float mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public void desenharShape(ShapeRenderer sr) {
        sr.setColor(corFundo);
        // Lógica de retângulo com cantos arredondados
        sr.rect(x + raio, y, width - 2 * raio, height);
        sr.rect(x, y + raio, width, height - 2 * raio);
        sr.circle(x + raio, y + raio, raio); 
        sr.circle(x + width - raio, y + raio, raio); 
        sr.circle(x + raio, y + height - raio, raio); 
        sr.circle(x + width - raio, y + height - raio, raio); 
    }

    public void desenharTexto(SpriteBatch batch, BitmapFont fonte) {
        GlyphLayout layout = new GlyphLayout(fonte, texto);
        float textoX = x + (width / 2f) - (layout.width / 2f);
        float textoY = y + (height / 2f) + (layout.height / 2f);
        fonte.draw(batch, layout, textoX, textoY);
    }
}