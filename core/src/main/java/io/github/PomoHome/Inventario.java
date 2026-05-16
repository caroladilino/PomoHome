package io.github.PomoHome;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;

public class Inventario {
    private Array<Movel> meusMoveis;
    private boolean expandido = false;

    // Medidas do Painel
    private float painelWidth = 250f;
    private float x, y, width, height;
    
    // Medidas da Aba (Botão de abrir/fechar)
    private float abaWidth = 40f;
    private float abaHeight = 80f;
    private float abaX, abaY;

    public Inventario() {
        meusMoveis = new Array<>();
    }

    public void adicionarMovel(Movel movel) {
        meusMoveis.add(movel);
    }

    public void calcularLayout(float telaWidth, float telaHeight) {
        this.width = painelWidth;
        this.height = telaHeight;
        this.y = 0;

        // Se estiver expandido, o X vem para a esquerda. Se não, fica escondido fora da tela.
        this.x = expandido ? telaWidth - this.width : telaWidth;

        // A aba fica sempre grudada na borda esquerda do painel
        this.abaX = this.x - abaWidth;
        this.abaY = (telaHeight / 2f) - (abaHeight / 2f); // Fica no meio da tela na vertical
    }

    public void atualizarLogica(float mouseX, float mouseY, boolean clicou) {
        if (clicou) {
            // Checa se o jogador clicou na aba
            if (mouseX >= abaX && mouseX <= abaX + abaWidth &&
                mouseY >= abaY && mouseY <= abaY + abaHeight) {
                expandido = !expandido; // Alterna entre aberto e fechado
            }
        }
    }

    public void desenhar(ShapeRenderer sr, SpriteBatch batch, BitmapFont fonte) {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        
        // 1. Desenhar a Aba
        sr.setColor(Color.valueOf("#B0B59E")); // Um bege um pouco mais escuro
        sr.rect(abaX, abaY, abaWidth, abaHeight);

        // 2. Desenhar o Painel (se estiver expandido)
        if (expandido) {
            sr.setColor(Color.valueOf("#EAECD7")); // Mesma cor do painel principal
            sr.rect(x, y, width, height);

            // Desenhar uma linha separadora para a borda
            sr.setColor(Color.valueOf("#B0B59E"));
            sr.rect(x, 0, 5, height);

            // Desenhar os móveis do inventário em uma grade (Grid)
            float startX = x + 25f;
            float startY = height - 100f;
            float size = 60f; // Tamanho do quadradinho no inventário
            float espaco = 20f;
            
            int colunas = 2;
            int cont = 0;

            for (Movel m : meusMoveis) {
                float posX = startX + (cont % colunas) * (size + espaco);
                float posY = startY - (cont / colunas) * (size + espaco + 30f);

                sr.setColor(m.corRepresentativa);
                sr.rect(posX, posY, size, size);
                cont++;
            }
        }
        sr.end();

        // 3. Desenhar Textos
        batch.begin();
        // Desenha uma setinha na aba
        fonte.setColor(Color.BLACK);
        fonte.draw(batch, expandido ? ">" : "<", abaX + 12, abaY + (abaHeight/2) + 5);

        if (expandido) {
            fonte.draw(batch, "Inventario", x + 20, height - 30);
            
            // Desenha os nomes embaixo dos quadradinhos
            int colunas = 2;
            int cont = 0;
            for (Movel m : meusMoveis) {
                float posX = x + 25f + (cont % colunas) * (60f + 20f);
                float posY = height - 100f - (cont / colunas) * (60f + 20f + 30f);
                
                // Pega só a primeira palavra do móvel para caber no inventário
                String nomeCurto = m.nome.split(" ")[0]; 
                fonte.getData().setScale(0.8f); // Diminui a fonte um pouco
                fonte.draw(batch, nomeCurto, posX, posY - 5);
                fonte.getData().setScale(1.2f); // Volta ao normal
                cont++;
            }
        }
        batch.end();
    }
}