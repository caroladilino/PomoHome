package io.github.PomoHome;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Botao {
    public float x, y, width, height;
    public String texto;
    public Color corFundo;
    public float raio = 10f; // Arredondamento padrão

    public Botao(String texto, float width, float height, Color corFundo) {
        this.texto = texto;
        this.width = width;
        this.height = height;
        this.corFundo = corFundo;
    }

    // Atualiza a posição do botão na tela
    public void setPosicao(float x, float y) {
        this.x = x;
        this.y = y;
    }

    // A mágica: O próprio botão sabe se foi clicado!
    public boolean isClicado(float mouseX, float mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    // Desenha o fundo do botão
    public void desenharShape(ShapeRenderer sr) {
        sr.setColor(corFundo);
        sr.rect(x + raio, y, width - 2 * raio, height);
        sr.rect(x, y + raio, width, height - 2 * raio);
        sr.circle(x + raio, y + raio, raio);
        sr.circle(x + width - raio, y + raio, raio);
        sr.circle(x + raio, y + height - raio, raio);
        sr.circle(x + width - raio, y + height - raio, raio);
    }

    // Desenha o texto do botão
    public void desenharTexto(SpriteBatch batch, BitmapFont fonte) {
        GlyphLayout layout = new GlyphLayout(fonte, texto);
        float textoX = x + (width / 2f) - (layout.width / 2f);
        float textoY = y + (height / 2f) + (layout.height / 2f);
        fonte.draw(batch, layout, textoX, textoY);
    }
}