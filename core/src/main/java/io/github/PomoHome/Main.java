package io.github.PomoHome;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

public class Main extends ApplicationAdapter {
    // Ferramentas de Desenho
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont fonteTimer, fonteBotao;

    // --- ARQUITETURA MVC ---
    private Jogo jogo; // O Cérebro (Model)
    private Jogador jogadorLogado; // Model
    
    private CasaView casaView; // Telas (View)
    private LojaView lojaView;
    private InventarioView inventarioView;

    // --- VARIÁVEIS DE ESTADO DA INTERFACE ---
    private enum EstadoTimer { PADRAO, EDITANDO, RODANDO, PAUSADO }
    private EstadoTimer estadoAtual = EstadoTimer.PADRAO;
    private enum ModoPainel { TIMER, LOJA }
    private ModoPainel viewAtual = ModoPainel.TIMER;
    private Movel movelNaMao = null; // Dragging
    
    // UI Local
    private Botao btnEsq, btnDir, btnCentro, btnMais, btnMenos, btnAbrirLoja, btnSairLoja;
    private float timerCentroX, timerCentroY, raioExternoTimer, raioInternoTimer;
    private float controlX, controlY, controlWidth, controlHeight, raioControle;

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true); 
        
        fonteTimer = new BitmapFont();
        fonteTimer.getData().setScale(3f); 
        fonteTimer.setColor(Color.BLACK);

        fonteBotao = new BitmapFont();
        fonteBotao.getData().setScale(1.2f);
        fonteBotao.setColor(Color.BLACK);

        // 1. INICIALIZA O MODELO (Dados e Regras)
        jogadorLogado = new Jogador("User1", new Casa("Minha Casa"));
        jogo = new Jogo(jogadorLogado); 
        
        // 2. INICIALIZA AS VISÕES (Telas)
        casaView = new CasaView(jogadorLogado);
        lojaView = new LojaView();
        inventarioView = new InventarioView(); 

        // Configuração dos Botões do Timer
        Color rosa = Color.valueOf("#E58F8F");
        btnEsq = new Botao("INICIAR", 140, 45, rosa);
        btnDir = new Botao("EDITAR", 140, 45, rosa);
        btnCentro = new Botao("ACEITAR", 140, 45, rosa);
        btnMais = new Botao("+", 40, 40, rosa);
        btnMenos = new Botao("-", 40, 40, rosa);
        btnAbrirLoja = new Botao("LOJA", 140, 45, rosa);
        btnSairLoja = new Botao("SAIR", 140, 45, rosa);
        
        // Passa o teclado para a CasaView
        Gdx.input.setInputProcessor(casaView.inputNomeCasa);
    }

    @Override
    public void render() {
        calcularLayout();
        atualizarLogica();
        desenharTela();
    }

    private void calcularLayout() {
        float telaWidth = Gdx.graphics.getWidth();
        float telaHeight = Gdx.graphics.getHeight();
        float bordaCm = Gdx.graphics.getPpcX();

        inventarioView.calcularLayout(telaWidth, telaHeight);
        casaView.calcularLayout(telaWidth, telaHeight); 

        controlWidth = telaWidth * 0.40f; 
        controlHeight = telaHeight * 0.9f;
        controlX = telaWidth * 0.05f;
        controlY = (telaHeight - controlHeight) / 2f;
        raioControle = bordaCm * 2f;

        lojaView.calcularLayout(controlX, controlY, controlWidth, controlHeight);

        timerCentroX = controlX + (controlWidth / 2f);
        timerCentroY = controlY + controlHeight - 220f; 
        raioExternoTimer = 110f;
        raioInternoTimer = 85f;

        float btnY = timerCentroY - raioExternoTimer - 70f; 
        float espacamento = 20f;
        btnEsq.setPosicao(timerCentroX - btnEsq.width - (espacamento / 2f), btnY);
        btnDir.setPosicao(timerCentroX + (espacamento / 2f), btnY);
        btnCentro.setPosicao(timerCentroX - (btnCentro.width / 2f), btnY);
        btnMenos.setPosicao(timerCentroX - raioExternoTimer - btnMenos.width - 20f, timerCentroY - 20f);
        btnMais.setPosicao(timerCentroX + raioExternoTimer + 20f, timerCentroY - 20f);
        btnAbrirLoja.setPosicao(timerCentroX - (btnAbrirLoja.width / 2f), btnY - 70f);
        btnSairLoja.setPosicao(timerCentroX - (btnSairLoja.width / 2f), controlY + 40f);
    }

    private void atualizarLogica() {
        // 1. Atualiza as Regras Matemáticas do Jogo
        jogo.atualizarLogica(Gdx.graphics.getDeltaTime());
        casaView.update(Gdx.graphics.getDeltaTime());

        boolean clicou = Gdx.input.justTouched();
        if (clicou) {
            float mouseX = Gdx.input.getX();
            float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY(); 

            // --- LÓGICA DE ARRASTAR (DECORAÇÃO) ---
            if (movelNaMao == null) {
                // Tenta pegar do inventário
                movelNaMao = inventarioView.getMovelClicado(mouseX, mouseY, jogadorLogado);
                if (movelNaMao != null) {
                    jogadorLogado.inventario.remove(movelNaMao); // Remove da lista do modelo
                    casaView.limparSelecao();
                    Gdx.input.setInputProcessor(casaView.inputNomeCasa);
                } else {
                    // Clicou na casa de mão vazia (Remove ou edita nome)
                    casaView.atualizarLogicaDeCliques(mouseX, mouseY, clicou);
                }
            } else {
                // Tem algo na mão, tenta devolver pro inventário ou soltar na casa
                if (inventarioView.isClicadoNoPainel(mouseX, mouseY)) {
                    jogadorLogado.inventario.add(movelNaMao);
                    movelNaMao = null; 
                } 
                else if (casaView.tentarColocarMovel(mouseX, mouseY, movelNaMao)) {
                    movelNaMao = null; 
                }
            }

            // Atualiza clique na aba do Inventário
            inventarioView.atualizarLogica(mouseX, mouseY, clicou);

            // --- LÓGICA DA INTERFACE ESQUERDA (BOTÕES E LOJA) ---
            if (movelNaMao == null) {
                if (viewAtual == ModoPainel.TIMER) {
                    if (estadoAtual == EstadoTimer.PADRAO && btnAbrirLoja.isClicado(mouseX, mouseY)) {
                        viewAtual = ModoPainel.LOJA;
                        Gdx.input.setInputProcessor(null); 
                    }

                    switch (estadoAtual) {
                        case PADRAO:
                            if (btnEsq.isClicado(mouseX, mouseY)) {
                                estadoAtual = EstadoTimer.RODANDO;
                                jogo.timer.rodando = true;
                            } else if (btnDir.isClicado(mouseX, mouseY)) {
                                estadoAtual = EstadoTimer.EDITANDO; 
                            }
                            break;
                        case EDITANDO:
                            if (btnCentro.isClicado(mouseX, mouseY)) {
                                estadoAtual = EstadoTimer.PADRAO; 
                            } else if (btnMenos.isClicado(mouseX, mouseY) && jogo.timer.tempoCiclo > 300) { 
                                jogo.timer.tempoCiclo -= 300; 
                                jogo.timer.resetar(); 
                            } else if (btnMais.isClicado(mouseX, mouseY) && jogo.timer.tempoCiclo < 5700) { 
                                jogo.timer.tempoCiclo += 300; 
                                jogo.timer.resetar(); 
                            }
                            break;
                        case RODANDO:
                        case PAUSADO:
                            if (btnEsq.isClicado(mouseX, mouseY)) {
                                jogo.timer.iniciarOuPausar();
                                estadoAtual = jogo.timer.rodando ? EstadoTimer.RODANDO : EstadoTimer.PAUSADO;
                            } else if (btnDir.isClicado(mouseX, mouseY)) { 
                                estadoAtual = EstadoTimer.PADRAO; 
                                jogo.timer.resetar(); 
                            }
                            break;
                    }

                } else if (viewAtual == ModoPainel.LOJA) {
                    if (btnSairLoja.isClicado(mouseX, mouseY)) {
                        viewAtual = ModoPainel.TIMER; 
                        Gdx.input.setInputProcessor(casaView.inputNomeCasa); 
                    } else {
                        // View captura o clique, Jogo aprova a compra!
                        ItemLoja item = lojaView.getItemClicado(mouseX, mouseY, jogo.loja);
                        if (item != null) {
                            jogo.loja.comprarItem(jogadorLogado, item); 
                        }
                    }
                }
            }
        }
        
        // Reset visual do Timer se a classe Timer avisar que acabou
        if (!jogo.timer.rodando && jogo.timer.tempoAtual <= 0) {
            estadoAtual = EstadoTimer.PADRAO;
            jogo.timer.resetar();
        }

        // Textos Dinâmicos dos botões
        if (estadoAtual == EstadoTimer.RODANDO || estadoAtual == EstadoTimer.PAUSADO) {
            btnEsq.texto = (estadoAtual == EstadoTimer.RODANDO) ? "PAUSAR" : "RETOMAR";
            btnDir.texto = "CANCELAR";
        } else {
            btnEsq.texto = "INICIAR";
            btnDir.texto = "EDITAR";
        }
    }

    private void desenharTela() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        // =================================================================
        // CAMADA 1: APENAS FORMAS (ShapeRenderer)
        // =================================================//==============
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        shapeRenderer.setColor(Color.PINK);
        desenharRetangulo(controlX, controlY, controlWidth, controlHeight, raioControle);
        shapeRenderer.setColor(Color.valueOf("#EDE8D8"));
        float borda = Gdx.graphics.getPpcX();
        desenharRetangulo(controlX + borda, controlY + borda, controlWidth - (borda * 2), controlHeight - (borda * 2), Math.max(0, raioControle - borda));

        casaView.desenharShape(shapeRenderer); 

        if (viewAtual == ModoPainel.TIMER) {
            shapeRenderer.setColor(Color.valueOf("#D3D3D3"));
            shapeRenderer.circle(timerCentroX, timerCentroY, raioExternoTimer);
            float proporcao = jogo.timer.tempoAtual / jogo.timer.tempoCiclo;
            if (proporcao > 0) {
                shapeRenderer.setColor(Color.valueOf("#696969"));
                shapeRenderer.arc(timerCentroX, timerCentroY, raioExternoTimer, 90, -(proporcao * 360f), 100);
            }
            shapeRenderer.setColor(Color.valueOf("#EDE8D8"));
            shapeRenderer.circle(timerCentroX, timerCentroY, raioInternoTimer);

            if (estadoAtual == EstadoTimer.PADRAO) {
                btnEsq.desenharShape(shapeRenderer);
                btnDir.desenharShape(shapeRenderer);
                btnAbrirLoja.desenharShape(shapeRenderer);
            } else if (estadoAtual == EstadoTimer.EDITANDO) {
                btnCentro.desenharShape(shapeRenderer);
                btnMenos.desenharShape(shapeRenderer);
                btnMais.desenharShape(shapeRenderer);
            } else {
                btnEsq.desenharShape(shapeRenderer);
                btnDir.desenharShape(shapeRenderer);
            }
        } else if (viewAtual == ModoPainel.LOJA) {
            // CORREÇÃO AQUI: Chama apenas a parte de formas da loja!
            lojaView.desenharShape(shapeRenderer, jogo.loja); 
            btnSairLoja.desenharShape(shapeRenderer);
        }

        // Saldo do Jogador
        shapeRenderer.setColor(Color.valueOf("#FFD700")); 
        shapeRenderer.circle(50, Gdx.graphics.getHeight() - 50, 30);
        
        if (movelNaMao != null) {
            float mouseX = Gdx.input.getX();
            float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
            shapeRenderer.setColor(Color.valueOf("#8B9BB4"));
            shapeRenderer.rect(mouseX - 30f, mouseY - 30f, 60f, 60f); 
        }

        shapeRenderer.end(); // FECHA o renderizador de formas com segurança!

        // =================================================================
        // CAMADA 2: APENAS TEXTOS E IMAGENS (SpriteBatch)
        // =================================================================
        batch.begin();
        fonteBotao.setColor(Color.BLACK);
        fonteBotao.draw(batch, "$" + jogadorLogado.saldo, 90, Gdx.graphics.getHeight() - 40);

        if (viewAtual == ModoPainel.TIMER) {
            int minutes = (int) (jogo.timer.tempoAtual / 60);
            int seconds = (int) (jogo.timer.tempoAtual % 60);
            String textoTimer = String.format("%02d:%02d", minutes, seconds);
            GlyphLayout layoutTimer = new GlyphLayout(fonteTimer, textoTimer);
            fonteTimer.draw(batch, layoutTimer, timerCentroX - (layoutTimer.width / 2f), timerCentroY + (layoutTimer.height / 2f));

            if (estadoAtual == EstadoTimer.PADRAO) {
                btnEsq.desenharTexto(batch, fonteBotao);
                btnDir.desenharTexto(batch, fonteBotao);
                btnAbrirLoja.desenharTexto(batch, fonteBotao);
            } else if (estadoAtual == EstadoTimer.EDITANDO) {
                btnCentro.desenharTexto(batch, fonteBotao);
                btnMenos.desenharTexto(batch, fonteBotao);
                btnMais.desenharTexto(batch, fonteBotao);
            } else {
                btnEsq.desenharTexto(batch, fonteBotao);
                btnDir.desenharTexto(batch, fonteBotao);
            }
        } else if (viewAtual == ModoPainel.LOJA) {
            // CORREÇÃO AQUI: Chama a parte de textos da loja dentro do batch!
            lojaView.desenharTexto(batch, fonteBotao, jogo.loja, jogadorLogado);
            btnSairLoja.desenharTexto(batch, fonteBotao);
        }

        casaView.desenharTexto(batch, fonteBotao);
        batch.end();

        // O inventário gerencia seu próprio ciclo interno de begin/end
        inventarioView.desenhar(shapeRenderer, batch, fonteBotao, jogadorLogado);
    }
    
    private void desenharRetangulo(float x, float y, float width, float height, float radius) {
        shapeRenderer.rect(x + radius, y, width - 2 * radius, height);
        shapeRenderer.rect(x, y + radius, width, height - 2 * radius);
        shapeRenderer.circle(x + radius, y + radius, radius); 
        shapeRenderer.circle(x + width - radius, y + radius, radius); 
        shapeRenderer.circle(x + radius, y + height - radius, radius); 
        shapeRenderer.circle(x + width - radius, y + height - radius, radius); 
    }

    @Override
    public void dispose() {
        batch.dispose(); 
        shapeRenderer.dispose();
        fonteTimer.dispose(); 
        fonteBotao.dispose();
    }
}