package io.github.PomoHome;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Polygon;

public class EspacoMovel {
    public Polygon poligono; // Guarda o formato exato do losango isométrico
    public float centroX, centroY; // Para desenhar o texto no meio
    public Movel movelColocado;

    public EspacoMovel(float[] vertices, float centroX, float centroY) {
        this.poligono = new Polygon(vertices);
        this.centroX = centroX;
        this.centroY = centroY;
        this.movelColocado = null;
    }

    public boolean isClicado(float mouseX, float mouseY) {
        // A mágica do LibGDX: ele sabe calcular se o clique foi DENTRO do losango!
        return poligono.contains(mouseX, mouseY);
    }

    public boolean estaVazio() {
        return movelColocado == null;
    }

    public void desenharShape(ShapeRenderer sr) {
        float[] v = poligono.getVertices();
        
        if (estaVazio()) {
            sr.setColor(Color.valueOf("#4A5568")); // Linhas do grid vazias
            sr.set(ShapeRenderer.ShapeType.Line);
            sr.polygon(v);
            sr.set(ShapeRenderer.ShapeType.Filled);
        } else {
            // O ShapeRenderer só preenche triângulos e retângulos, então para
            // preencher um losango isométrico, nós desenhamos 2 triângulos colados!
            sr.setColor(movelColocado.corRepresentativa);
            sr.triangle(v[0], v[1], v[2], v[3], v[6], v[7]); // Metade esquerda
            sr.triangle(v[2], v[3], v[4], v[5], v[6], v[7]); // Metade direita
            
            // Desenha a bordinha preta
            sr.setColor(Color.valueOf("#1A202C"));
            sr.set(ShapeRenderer.ShapeType.Line);
            sr.polygon(v);
            sr.set(ShapeRenderer.ShapeType.Filled);
        }
    }

    public void desenharTexto(SpriteBatch batch, BitmapFont fonte) {
        if (!estaVazio()) {
            fonte.setColor(Color.WHITE);
            
            float originalScaleX = fonte.getScaleX();
            float originalScaleY = fonte.getScaleY();
            
            // Deixa a fonte pequenininha pra caber no losango
            fonte.getData().setScale(0.55f);
            
            GlyphLayout layout = new GlyphLayout(fonte, movelColocado.nome);
            float textoX = centroX - (layout.width / 2f);
            float textoY = centroY + (layout.height / 2f);
            
            fonte.draw(batch, layout, textoX, textoY);
            
            fonte.getData().setScale(originalScaleX, originalScaleY);
            fonte.setColor(Color.BLACK);
        }
    }
}