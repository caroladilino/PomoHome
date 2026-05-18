package io.github.PomoHome;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;

public class Casa {
    private Array<EspacoMovel> grid;
    private final int MATRIZ_TAMANHO = 8; // Continua sendo 8x8 (não adicionamos mais quadrados)
    
    // UI
    public CaixaTexto inputNomeCasa;
    private Botao btnRemoverMovel;
    private EspacoMovel slotSelecionado = null;
    
    // Dados de posicionamento para o Input de texto
    private float baseGridX, baseGridY;

    public Casa() {
        grid = new Array<>();
        for (int i = 0; i < MATRIZ_TAMANHO * MATRIZ_TAMANHO; i++) {
            grid.add(new EspacoMovel(new float[8], 0, 0));
        }

        inputNomeCasa = new CaixaTexto(180f, 40f, "Minha Casa");
        btnRemoverMovel = new Botao("X", 30f, 30f, Color.RED);
        btnRemoverMovel.raio = 15f; 
    }

    public void calcularLayout(float telaWidth, float telaHeight) {
        float centroDireitoX = telaWidth * 0.75f;
        float centroDireitoY = telaHeight / 2f;

        // ==========================================
        // O "ZOOM" ACONTECE AQUI:
        // Dobramos o tamanho do azulejo isométrico
        // ==========================================
        float tileWidth = 100f;  // Antes era 50f
        float tileHeight = 50f;  // Antes era 25f

        // Recalculando o ponto de origem para o grid maior ficar bem centralizado
        float originX = centroDireitoX;
        float originY = centroDireitoY - 200f; // Puxamos mais para baixo para compensar o tamanho novo
        
        baseGridX = originX;
        baseGridY = originY;

        int index = 0;
        for (int linha = 0; linha < MATRIZ_TAMANHO; linha++) {
            for (int coluna = 0; coluna < MATRIZ_TAMANHO; coluna++) {
                
                float px = originX + (coluna - linha) * (tileWidth / 2f);
                float py = originY + (coluna + linha) * (tileHeight / 2f);

                float[] vertices = new float[]{
                    px - tileWidth/2f, py,                 
                    px, py - tileHeight/2f,                 
                    px + tileWidth/2f, py,                  
                    px, py + tileHeight/2f                  
                };
                
                EspacoMovel slot = grid.get(index);
                slot.poligono.setVertices(vertices);
                slot.centroX = px;
                slot.centroY = py;
                index++;
            }
        }

        // Reposiciona a caixa de texto para ficar abaixo do grid gigante
        inputNomeCasa.setPosicao(baseGridX - (inputNomeCasa.width / 2f), baseGridY - 70f);
    }

    public void atualizarLogica(float mouseX, float mouseY, boolean clicou, Inventario inventario) {
        if (!clicou) return;

        if (slotSelecionado != null && btnRemoverMovel.isClicado(mouseX, mouseY)) {
            inventario.adicionarMovel(slotSelecionado.movelColocado); 
            slotSelecionado.movelColocado = null; 
            slotSelecionado = null; 
            return; 
        }

        if (inputNomeCasa.isClicado(mouseX, mouseY)) {
            inputNomeCasa.ativo = true; 
            return;
        } else {
            inputNomeCasa.ativo = false; 
        }

        boolean clicouNaCasa = false;
        for (int i = grid.size - 1; i >= 0; i--) {
            EspacoMovel slot = grid.get(i);
            if (slot.isClicado(mouseX, mouseY)) {
                clicouNaCasa = true;
                if (!slot.estaVazio()) {
                    slotSelecionado = slot;
                    // Bota o X logo acima do móvel (ajustei a altura por causa do zoom)
                    btnRemoverMovel.setPosicao(slot.centroX - (btnRemoverMovel.width / 2f), slot.centroY + 25f);
                } else {
                    slotSelecionado = null; 
                }
                break;
            }
        }

        if (!clicouNaCasa) {
            slotSelecionado = null;
        }
    }

    public boolean tentarColocarMovel(float mouseX, float mouseY, Movel movelNaMao) {
        for (int i = grid.size - 1; i >= 0; i--) {
            EspacoMovel slot = grid.get(i);
            if (slot.isClicado(mouseX, mouseY) && slot.estaVazio()) {
                slot.movelColocado = movelNaMao; 
                slotSelecionado = null; 
                return true; 
            }
        }
        return false; 
    }

    public void limparSelecao() {
        slotSelecionado = null;
    }

    public void desenharShape(ShapeRenderer sr) {
        for (EspacoMovel slot : grid) {
            slot.desenharShape(sr);
        }
        inputNomeCasa.desenharShape(sr);

        if (slotSelecionado != null) {
            btnRemoverMovel.desenharShape(sr);
        }
    }

    public void desenharTexto(SpriteBatch batch, BitmapFont fonte) {
        for (EspacoMovel slot : grid) {
            slot.desenharTexto(batch, fonte);
        }
        inputNomeCasa.desenharTexto(batch, fonte);

        if (slotSelecionado != null) {
            btnRemoverMovel.desenharTexto(batch, fonte);
        }
    }
}