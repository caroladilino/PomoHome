package io.github.PomoHome;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class HistoricoView {
    public boolean expandido = false;
    private float painelWidth = 450f; 
    private float x, y, width, height;
    
    //configurações da aba circular
    private float abaRaio = 30f;
    private float abaCentroX, abaCentroY;

    private Historico historico;
    private String[] linhasHistorico;

    public HistoricoView() {
        this.historico = new Historico();
        carregarTextoDoArquivo();
    }

    public void carregarTextoDoArquivo() {
        var arquivoTxt = Gdx.files.local("historico.txt");
        if (arquivoTxt.exists()) {
            String conteudo = arquivoTxt.readString();
            String[] todasAsLinhas = conteudo.split("\n");

            //listas para separar as estatísticas das infos de cada ciclo
            java.util.ArrayList<String> ciclos = new java.util.ArrayList<>();
            java.util.ArrayList<String> estatisticas = new java.util.ArrayList<>();

            for (String linha : todasAsLinhas) {
                if (linha.trim().isEmpty()) continue;
                
                if (linha.contains("Ciclo de estudos de")) {
                    ciclos.add(linha);
                } else {
                    //tudo que não é ciclo, vai para estatísticas
                    estatisticas.add(linha);
                }
            }

            //Pega só os 5 ciclos mais recentes
            java.util.ArrayList<String> linhasFinais = new java.util.ArrayList<>();
            linhasFinais.add("--- CICLOS RECENTES ---");
            
            int exibidos = 0;
            for (int i = ciclos.size() - 1; i >= 0; i--) {
                if (exibidos >= 5) break;
                linhasFinais.add(ciclos.get(i));
                exibidos++;
            }
            
            if (ciclos.isEmpty()) {
                //caso esteja vazio (sem nenhum ciclo)
                linhasFinais.add("Nenhum ciclo concluído ainda.");
            }

            //adiciona as estatísticas abaixo dos ciclos recentes
            linhasFinais.addAll(estatisticas);

            //converte de volta para o Array esperado pelo loop de desenho
            this.linhasHistorico = linhasFinais.toArray(new String[0]);
        } else {
            this.linhasHistorico = new String[]{"Nenhum histórico registrado ainda."};
        }
    }

    public void calcularLayout(float telaWidth, float telaHeight) {
        this.width = painelWidth;
        this.height = 400f; 

        float margem = 45f; 

        if (expandido) {
            this.x = telaWidth - this.width;
            this.y = 0;
            this.abaCentroX = this.x + (this.width / 2f);
            this.abaCentroY = this.y + this.height;
        } else {
            this.x = telaWidth - this.width;
            this.y = -this.height;
            this.abaCentroX = telaWidth - margem;
            this.abaCentroY = margem;
        }
    }

    public void atualizarLogica(float mouseX, float mouseY, boolean clicou) {
        if (clicou) {
            float dx = mouseX - abaCentroX;
            float dy = mouseY - abaCentroY;
            float distanciaAoQuadrado = (dx * dx) + (dy * dy);
            float raioAoQuadrado = abaRaio * abaRaio;

            if (distanciaAoQuadrado <= raioAoQuadrado) {
                expandido = !expandido;
                
                if (expandido) {
                    carregarTextoDoArquivo();
                }
            }
        }
    }

    public boolean isClicadoNoPainel(float mouseX, float mouseY) {
        if (!expandido) return false;
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public void desenhar(ShapeRenderer sr, SpriteBatch batch, BitmapFont fonte, Jogador jogador) {
        // --- BLOCO 1: DESENHO DAS FORMAS DE FUNDO (SHAPERENDERER) ---
        sr.begin(ShapeRenderer.ShapeType.Filled);
        
        // 1. Desenha o círculo da aba (funciona tanto aberto quanto fechado)
        sr.setColor(Color.valueOf("#B0B59E")); 
        sr.circle(abaCentroX, abaCentroY, abaRaio);

        // 2. Se estiver aberto, desenha o retângulo do painel de fundo
        if (expandido) {
            sr.setColor(Color.valueOf("#EAECD7")); 
            sr.rect(x, y, width, height);
            
            // Redesenha a bolinha por cima do painel para o botão de fechar ficar visível no topo
            sr.setColor(Color.valueOf("#B0B59E")); 
            sr.circle(abaCentroX, abaCentroY, abaRaio);
        }
        sr.end();

        // --- BLOCO 2: DESENHO DOS TEXTOS (SPRITEBATCH) ---
        batch.begin();
        fonte.setColor(Color.BLACK);

        String seta = expandido ? "v" : "^";
        fonte.draw(batch, seta, abaCentroX - 6f, abaCentroY + 10f);

        if (expandido) {
            fonte.getData().setScale(1.2f); 
            fonte.draw(batch, "HISTÓRICO DE ESTUDOS", x + 20, y + height - 30);
            
            fonte.getData().setScale(0.85f); 
            float iniciarOndeY = y + height - 80f; 
            float espacamentoEntreLinhas = 25f; 

            for (int i = 0; i < linhasHistorico.length; i++) {
                float posY = iniciarOndeY - (i * espacamentoEntreLinhas);
                
                if (posY < y + 30) {
                    fonte.draw(batch, "... e mais registros acima ...", x + 20, posY);
                    break;
                }
                
                if (linhasHistorico[i].contains("--- ESTATÍSTICAS ---")) {
                    fonte.setColor(Color.valueOf("#7A806A")); 
                } else {
                    fonte.setColor(Color.BLACK); //texto normal seja preto
                }

                fonte.draw(batch, linhasHistorico[i], x + 20, posY);
            }
            
            //reseta as configurações padrão da fonte
            fonte.getData().setScale(1.2f); 
            fonte.setColor(Color.BLACK);
        }
        batch.end();
    }
}