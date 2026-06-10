package io.github.PomoHome;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class LojaView {
    private float startX, startY;
    private float itemSize = 80f; 
    private float espacoX = 30f;
    private float espacoY = 60f; 
    private int colunas = 2;

    public void calcularLayout(float painelX, float painelY, float painelWidth, float painelHeight) {
        float larguraTotalGrid = (itemSize * colunas) + (espacoX * (colunas - 1));
        this.startX = painelX + (painelWidth / 2f) - (larguraTotalGrid / 2f);
        this.startY = painelY + painelHeight - 120f; 
    }

    public ItemLoja getItemClicado(float mouseX, float mouseY, Loja lojaLogica) {
        for (int i = 0; i < lojaLogica.itens.size(); i++) {
            ItemLoja item = lojaLogica.itens.get(i);
            if (!item.disponivel) continue; 

            float px = startX + (i % colunas) * (itemSize + espacoX);
            float py = startY - (i / colunas) * (itemSize + espacoY);

            if (mouseX >= px && mouseX <= px + itemSize && mouseY >= py && mouseY <= py + itemSize) {
                return item;
            }
        }
        return null;
    }

    // --- NOVO PASSE: Desenha apenas os quadrados (Roda dentro do begin do ShapeRenderer) ---
    public void desenharShape(ShapeRenderer sr, Loja lojaLogica) {
        for (int i = 0; i < lojaLogica.itens.size(); i++) {
            ItemLoja item = lojaLogica.itens.get(i);
            if (!item.disponivel) continue;

            float px = startX + (i % colunas) * (itemSize + espacoX);
            float py = startY - (i / colunas) * (itemSize + espacoY);
            
            sr.setColor(Color.valueOf("#B0B59E")); 
            sr.rect(px, py, itemSize, itemSize);
        }
    }

    // --- NOVO PASSE: Desenha apenas as fontes (Roda dentro do begin do SpriteBatch) ---
    public void desenharTexto(SpriteBatch batch, BitmapFont fonte, Loja lojaLogica, Jogador jogador) {
        for (int i = 0; i < lojaLogica.itens.size(); i++) {
            ItemLoja item = lojaLogica.itens.get(i);
            if (!item.disponivel) continue;

            Movel m = item.movel;
            float px = startX + (i % colunas) * (itemSize + espacoX);
            float py = startY - (i / colunas) * (itemSize + espacoY);

            GlyphLayout lNome = new GlyphLayout(fonte, m.nome);
            fonte.setColor(Color.BLACK);
            fonte.draw(batch, lNome, px + (itemSize/2f) - (lNome.width/2f), py - 5f);

            GlyphLayout lPreco = new GlyphLayout(fonte, "$" + m.preco);
            if (m.preco > jogador.saldo) fonte.setColor(Color.RED);
            fonte.draw(batch, lPreco, px + (itemSize/2f) - (lPreco.width/2f), py - 35f);
            fonte.setColor(Color.BLACK);
        }
    }
}