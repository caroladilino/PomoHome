package io.github.PomoHome;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

// O InputAdapter permite que essa classe "escute" o teclado do computador
public class CaixaTexto extends InputAdapter {
    public float x, y, width, height;
    public String texto;
    public boolean ativo = false; // Se está selecionada (em foco)
    
    // Variáveis para animar o cursor piscando
    private float tempoCursor = 0f;
    private boolean mostrarCursor = false;
    private int limiteCaracteres = 15;

    public CaixaTexto(float width, float height, String textoInicial) {
        this.width = width;
        this.height = height;
        this.texto = textoInicial;
    }

    public void setPosicao(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public boolean isClicado(float mouseX, float mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    // --- MÁGICA DO TECLADO ---
    // Este método é chamado automaticamente pelo LibGDX toda vez que você digita uma letra
    @Override
    public boolean keyTyped(char character) {
        if (!ativo) return false; // Ignora o teclado se a caixa não estiver clicada

        // Se apertar BackSpace (Apagar)
        if (character == '\b' && texto.length() > 0) {
            texto = texto.substring(0, texto.length() - 1);
        }
        // Se apertar Enter (Finaliza a edição)
        else if (character == '\n' || character == '\r') {
            ativo = false;
        }
        // Aceita letras, números e espaços (Tabela ASCII entre 32 e 126)
        else if (character >= 32 && character <= 126 && texto.length() < limiteCaracteres) {
            texto += character;
        }
        return true;
    }

    // --- DESENHO NA TELA ---
    public void desenharShape(ShapeRenderer sr) {
        // Cor do fundo: Branco se estiver digitando, Bege escuro se estiver inativo
        sr.setColor(Color.valueOf(ativo ? "#FFFFFF" : "#B0B59E"));
        desenharRetanguloArredondado(sr, x, y, width, height, 8f);

        // Se estiver ativo, desenha uma borda rosa em volta
        if (ativo) {
            sr.setColor(Color.valueOf("#E58F8F"));
            sr.set(ShapeRenderer.ShapeType.Line);
            desenharRetanguloArredondado(sr, x, y, width, height, 8f);
            sr.set(ShapeRenderer.ShapeType.Filled);
        }
    }

    public void desenharTexto(SpriteBatch batch, BitmapFont fonte) {
        // Lógica do cursor piscando (alterna a cada 0.5 segundos)
        if (ativo) {
            tempoCursor += Gdx.graphics.getDeltaTime();
            if (tempoCursor >= 0.5f) {
                mostrarCursor = !mostrarCursor;
                tempoCursor = 0f;
            }
        } else {
            mostrarCursor = false;
        }

        fonte.setColor(Color.BLACK);
        String textoExibicao = texto + (mostrarCursor ? "|" : ""); // Adiciona o pipe se o cursor estiver visível
        
        GlyphLayout layout = new GlyphLayout(fonte, textoExibicao);
        float textoX = x + (width / 2f) - (layout.width / 2f);
        float textoY = y + (height / 2f) + (layout.height / 2f);
        
        fonte.draw(batch, layout, textoX, textoY);
    }

    private void desenharRetanguloArredondado(ShapeRenderer sr, float x, float y, float width, float height, float radius) {
        sr.rect(x + radius, y, width - 2 * radius, height);
        sr.rect(x, y + radius, width, height - 2 * radius);
        sr.circle(x + radius, y + radius, radius); 
        sr.circle(x + width - radius, y + radius, radius); 
        sr.circle(x + radius, y + height - radius, radius); 
        sr.circle(x + width - radius, y + height - radius, radius); 
    }
}