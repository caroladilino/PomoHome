package io.github.PomoHome;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;

public class Loja {
    private Array<Movel> catalogo;

    // Configurações do Grid
    private float startX, startY;
    private float itemSize = 80f; // Tamanho do quadrado de cada móvel
    private float espacoX = 30f;
    private float espacoY = 60f; // Espaço maior no Y para caber os textos
    private int colunas = 2;

    public Loja() {
        catalogo = new Array<>();
        catalogo.add(new Movel("Cama", 150, Color.BROWN));
        catalogo.add(new Movel("Cadeira", 80, Color.RED));
        catalogo.add(new Movel("Mesa", 120, Color.TAN));
        catalogo.add(new Movel("Planta", 30, Color.GREEN));
        catalogo.add(new Movel("Tapete", 50, Color.PURPLE));
    }

    // Calcula de onde o Grid deve começar a ser desenhado (Baseado no contêiner)
    public void calcularLayout(float painelX, float painelY, float painelWidth, float painelHeight) {
        float larguraTotalGrid = (itemSize * colunas) + (espacoX * (colunas - 1));
        this.startX = painelX + (painelWidth / 2f) - (larguraTotalGrid / 2f);
        this.startY = painelY + painelHeight - 120f; // Começa a desenhar perto do topo do painel
    }

    // Checa todos os itens da grade para ver se o mouse clicou em algum deles
    public Movel getMovelClicado(float mouseX, float mouseY) {
        for (int i = 0; i < catalogo.size; i++) {
            float px = startX + (i % colunas) * (itemSize + espacoX);
            float py = startY - (i / colunas) * (itemSize + espacoY);

            // Verifica colisão com o quadrado
            if (mouseX >= px && mouseX <= px + itemSize && 
                mouseY >= py && mouseY <= py + itemSize) {
                return catalogo.get(i);
            }
        }
        return null;
    }

    // Remove do catálogo (Chamado pela Main após gastar o dinheiro)
    public void removerMovel(Movel m) {
        catalogo.removeValue(m, true);
    }

    public void desenharShape(ShapeRenderer sr) {
        for (int i = 0; i < catalogo.size; i++) {
            float px = startX + (i % colunas) * (itemSize + espacoX);
            float py = startY - (i / colunas) * (itemSize + espacoY);

            sr.setColor(catalogo.get(i).corRepresentativa);
            sr.rect(px, py, itemSize, itemSize);
        }
    }

    public void desenharTexto(SpriteBatch batch, BitmapFont fonte, int saldoJogador) {
        if (catalogo.isEmpty()) {
            fonte.setColor(Color.DARK_GRAY);
            GlyphLayout layout = new GlyphLayout(fonte, "Loja Vazia!");
            // Desenha no centro do espaço da loja
            fonte.draw(batch, layout, startX, startY); 
            fonte.setColor(Color.BLACK);
            return;
        }

        for (int i = 0; i < catalogo.size; i++) {
            Movel m = catalogo.get(i);
            float px = startX + (i % colunas) * (itemSize + espacoX);
            float py = startY - (i / colunas) * (itemSize + espacoY);

            // Desenha o Nome
            GlyphLayout lNome = new GlyphLayout(fonte, m.nome);
            fonte.setColor(Color.BLACK);
            fonte.draw(batch, lNome, px + (itemSize/2f) - (lNome.width/2f), py - 5f);

            // Desenha o Preço (Vermelho se não puder comprar)
            GlyphLayout lPreco = new GlyphLayout(fonte, "$" + m.preco);
            if (m.preco > saldoJogador) {
                fonte.setColor(Color.RED);
            }
            fonte.draw(batch, lPreco, px + (itemSize/2f) - (lPreco.width/2f), py - 25f);
        }
        fonte.setColor(Color.BLACK); // Restaura a cor padrão
    }
}