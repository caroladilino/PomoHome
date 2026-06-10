package io.github.PomoHome;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Polygon;

public class EspacoMovel {
    public Polygon poligono;
    public float centroX, centroY;
    public Movel movelReferencia; // Referência ao dado puro do Movel

    public EspacoMovel(float[] vertices, float centroX, float centroY) {
        this.poligono = new Polygon(vertices);
        this.centroX = centroX;
        this.centroY = centroY;
        this.movelReferencia = null;
    }

    public boolean isClicado(float mouseX, float mouseY) {
        return poligono.contains(mouseX, mouseY);
    }

    public boolean estaVazio() {
        return movelReferencia == null;
    }

    public void desenharShape(ShapeRenderer sr) {
        float[] v = poligono.getVertices();
        
        if (estaVazio()) {
            sr.setColor(Color.valueOf("#4A5568")); // Cor da grade vazia
            sr.set(ShapeRenderer.ShapeType.Line);
            sr.polygon(v);
            sr.set(ShapeRenderer.ShapeType.Filled);
        } else {
            // Desenha a base colorida do móvel usando dois triângulos
            sr.setColor(Color.valueOf("#8B9BB4")); // Cor genérica de "ocupado"
            sr.triangle(v[0], v[1], v[2], v[3], v[6], v[7]); 
            sr.triangle(v[2], v[3], v[4], v[5], v[6], v[7]); 
            
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
            
            fonte.getData().setScale(0.55f);
            
            GlyphLayout layout = new GlyphLayout(fonte, movelReferencia.nome);
            float textoX = centroX - (layout.width / 2f);
            float textoY = centroY + (layout.height / 2f);
            
            fonte.draw(batch, layout, textoX, textoY);
            
            fonte.getData().setScale(originalScaleX, originalScaleY);
            fonte.setColor(Color.BLACK);
        }
    }
}