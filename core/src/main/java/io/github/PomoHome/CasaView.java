package io.github.PomoHome;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;

public class CasaView {
    public Array<EspacoMovel> gridVisual;
    private final int MATRIZ_TAMANHO = 8; 
    
    public CaixaTexto inputNomeCasa;
    public Botao btnRemoverMovel;
    public EspacoMovel slotSelecionado = null;
    
    private float casaX, casaY, casaSize;
    private float baseGridX, baseGridY;

    // Referências do Modelo
    private Casa casaLogica;
    private Jogador jogador;
    
    // UI Mensagens
    private String mensagemAviso = "";
    private Color corAviso = Color.WHITE;
    private float tempoAviso = 0f;

    public CasaView(Jogador jogador) {
        this.jogador = jogador;
        this.casaLogica = jogador.casa;

        gridVisual = new Array<>();
        for (int i = 0; i < MATRIZ_TAMANHO * MATRIZ_TAMANHO; i++) {
            gridVisual.add(new EspacoMovel(new float[8], 0, 0));
        }
        
        inputNomeCasa = new CaixaTexto(180f, 40f, casaLogica.nome, this);
        btnRemoverMovel = new Botao("X", 30f, 30f, Color.RED);
        btnRemoverMovel.raio = 15f; 
    }

    public void calcularLayout(float telaWidth, float telaHeight) {
        float centroDireitoX = telaWidth * 0.75f;
        float centroDireitoY = telaHeight / 2f;
        float tileWidth = 100f;  
        float tileHeight = 50f; 
        float originX = centroDireitoX; 
        float originY = centroDireitoY - 200f; 
        
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
                
                EspacoMovel slot = gridVisual.get(index);
                slot.poligono.setVertices(vertices);
                slot.centroX = px;
                slot.centroY = py;
                index++;
            }
        }
        inputNomeCasa.setPosicao(baseGridX - (inputNomeCasa.width / 2f), baseGridY - 60f);
    }

    public void update(float delta) {
        if (tempoAviso > 0) tempoAviso -= delta;
    }

    public void validarNovoNomeDaInterface(String novoNomeDigitado) {
        String novoNome = novoNomeDigitado.trim();
        if (novoNome.isEmpty() || novoNome.equals(casaLogica.nome) || !novoNome.matches("^[a-zA-Z0-9À-ÿ ]+$")) {
            inputNomeCasa.ativo = true;
            exibirMensagem("Nome invalido!", Color.RED);
        } else {
            casaLogica.alterarNome(novoNome);
            inputNomeCasa.texto = casaLogica.nome; 
            inputNomeCasa.ativo = false; 
            exibirMensagem("Nome atualizado!", Color.GREEN);
        }
    }

    private void exibirMensagem(String msg, Color cor) {
        this.mensagemAviso = msg;
        this.corAviso = cor;
        this.tempoAviso = 3f; 
    }

    public void atualizarLogicaDeCliques(float mouseX, float mouseY, boolean clicou) {
        if (!clicou) return;

        // Tenta remover móvel
        if (slotSelecionado != null && btnRemoverMovel.isClicado(mouseX, mouseY)) {
            Movel movelRemovido = slotSelecionado.movelReferencia;
            jogador.inventario.add(movelRemovido); // Volta pro inventário
            casaLogica.removerMovel(movelRemovido); // Remove do Modelo
            
            // Libera o espaço visual
            for (EspacoMovel slot : gridVisual) {
                if (slot.movelReferencia == movelRemovido) slot.movelReferencia = null;
            }
            slotSelecionado = null; 
            return; 
        }

        if (inputNomeCasa.isClicado(mouseX, mouseY)) {
            inputNomeCasa.ativo = true; 
            return;
        } else if (inputNomeCasa.ativo) {
            inputNomeCasa.ativo = false;
            inputNomeCasa.texto = casaLogica.nome;
        }

        boolean clicouNaCasa = false;
        for (int i = gridVisual.size - 1; i >= 0; i--) {
            EspacoMovel slot = gridVisual.get(i);
            if (slot.isClicado(mouseX, mouseY)) {
                clicouNaCasa = true;
                if (!slot.estaVazio()) {
                    slotSelecionado = slot;
                    btnRemoverMovel.setPosicao(slot.centroX - (btnRemoverMovel.width / 2f), slot.centroY + 25f);
                } else {
                    slotSelecionado = null; 
                }
                break;
            }
        }
        if (!clicouNaCasa) slotSelecionado = null;
    }

    public boolean tentarColocarMovel(float mouseX, float mouseY, Movel m) {
        int[] anchor = getSlotAnchorUnderMouse(mouseX, mouseY);
        if (anchor != null) {
            int rowAnchor = anchor[0];
            int colAnchor = anchor[1];
            
            // Verifica se a área cabe (Multislot)
            if (rowAnchor + m.widthInTiles > MATRIZ_TAMANHO || colAnchor + m.heightInTiles > MATRIZ_TAMANHO) return false;
            
            for (int r = rowAnchor; r < rowAnchor + m.widthInTiles; r++) {
                for (int c = colAnchor; c < colAnchor + m.heightInTiles; c++) {
                    int index = r * MATRIZ_TAMANHO + c;
                    if (!gridVisual.get(index).estaVazio()) return false; 
                }
            }
            
            // Reserva a área visual
            for (int r = rowAnchor; r < rowAnchor + m.widthInTiles; r++) {
                for (int c = colAnchor; c < colAnchor + m.heightInTiles; c++) {
                    int index = r * MATRIZ_TAMANHO + c;
                    gridVisual.get(index).movelReferencia = m;
                }
            }
            
            casaLogica.adicionarMovel(m); // Salva no Modelo
            slotSelecionado = null; 
            return true; 
        }
        return false; 
    }

    private int[] getSlotAnchorUnderMouse(float mouseX, float mouseY) {
        for (int row = MATRIZ_TAMANHO - 1; row >= 0; row--) {
            for (int col = MATRIZ_TAMANHO - 1; col >= 0; col--) {
                int index = row * MATRIZ_TAMANHO + col;
                EspacoMovel slot = gridVisual.get(index);
                if (slot.isClicado(mouseX, mouseY)) {
                    return new int[]{row, col, index};
                }
            }
        }
        return null;
    }

    public void limparSelecao() {
        slotSelecionado = null;
    }

    public void desenharShape(ShapeRenderer sr) {
        for (EspacoMovel slot : gridVisual) {
            slot.desenharShape(sr);
        }
        inputNomeCasa.desenharShape(sr);

        if (slotSelecionado != null) {
            btnRemoverMovel.desenharShape(sr);
        }
    }

    public void desenharTexto(SpriteBatch batch, BitmapFont fonte) {
        for (EspacoMovel slot : gridVisual) {
            slot.desenharTexto(batch, fonte);
        }
        inputNomeCasa.desenharTexto(batch, fonte);

        if (slotSelecionado != null) {
            btnRemoverMovel.desenharTexto(batch, fonte);
        }

        if (tempoAviso > 0) {
            fonte.setColor(corAviso);
            GlyphLayout layout = new GlyphLayout(fonte, mensagemAviso);
            fonte.draw(batch, layout, baseGridX - (layout.width / 2f), baseGridY - 70f);
            fonte.setColor(Color.BLACK);
        }
    }
}