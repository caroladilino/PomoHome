package io.github.PomoHome;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class GerenciadorMoedas {
    private int saldo;
    
    // Variáveis visuais (Posição e tamanho da caixinha de moedas)
    private float x, y, width, height;
    private float posMoedaX, posMoedaY, raioMoedaVisual;

    public GerenciadorMoedas() {
        this.saldo = 1000; // O jogador sempre começa com 0 (no futuro, podemos ler de um arquivo de save aqui)
    }

    // --- REGRAS DE NEGÓCIO (ECONOMIA) ---

    public void adicionarMoedas(int quantidade) {
        if (quantidade > 0) {
            this.saldo += quantidade;
        }
    }

    // Tenta gastar moedas. Retorna TRUE se comprou, FALSE se não tiver dinheiro suficiente.
    public boolean gastarMoedas(int valorItem) {
        if (this.saldo >= valorItem) {
            this.saldo -= valorItem;
            return true;
        }
        return false;
    }

    public int getSaldo() {
        return this.saldo;
    }

    // --- REGRAS DE INTERFACE VISUAL (UI) ---

    // Calcula onde a caixinha deve ficar (Canto superior direito)
    public void calcularLayout(float telaWidth, float telaHeight) {
        this.width = 120f;
        this.height = 40f;
        this.x = telaWidth - this.width - 20f; 
        this.y = telaHeight - this.height - 20f;
        
        this.posMoedaX = this.x + 20f; 
        this.posMoedaY = this.y + (this.height / 2f);
        this.raioMoedaVisual = 12f;
    }

    // Desenha o fundo bege e a moeda dourada
    public void desenharShape(ShapeRenderer sr) {
        sr.setColor(Color.valueOf("#EDE8D8"));
        desenharRetangulo(sr, x, y, width, height, 10f);
        
        sr.setColor(Color.GOLD);
        sr.circle(posMoedaX, posMoedaY, raioMoedaVisual);
    }

    // Desenha o texto com o valor do saldo
    public void desenharTexto(SpriteBatch batch, BitmapFont fonte) {
        GlyphLayout layout = new GlyphLayout(fonte, String.valueOf(saldo));
        float textoMoedaX = posMoedaX + raioMoedaVisual + 10f;
        float textoMoedaY = y + (height / 2f) + (layout.height / 2f);
        fonte.draw(batch, layout, textoMoedaX, textoMoedaY);
    }

    // Método utilitário interno para o fundo arredondado
    private void desenharRetangulo(ShapeRenderer sr, float x, float y, float w, float h, float r) {
        sr.rect(x + r, y, w - 2 * r, h);
        sr.rect(x, y + r, w, h - 2 * r);
        sr.circle(x + r, y + r, r);
        sr.circle(x + w - r, y + r, r);
        sr.circle(x + r, y + h - r, r);
        sr.circle(x + w - r, y + h - r, r);
    }
}