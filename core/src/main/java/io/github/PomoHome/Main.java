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
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont fonteTimer, fonteBotao;

    // --- VARIÁVEIS DE ESTADO E TELAS ---
    private enum Estado { PADRAO, EDITANDO, RODANDO, PAUSADO }
    private Estado estadoAtual = Estado.PADRAO;
    
    private enum ModoView { TIMER, LOJA } // <--- CONTROLA O QUE APARECE NO CONTÊINER
    private ModoView viewAtual = ModoView.TIMER;

    private float tempoConfigurado = 53 * 60; 
    private float tempoAtual = tempoConfigurado;

    // Sistemas do Jogo
    private GerenciadorMoedas banco;
    private Loja loja; 
    private Inventario inventario; 

    // UI
    private Botao btnEsq, btnDir, btnCentro, btnMais, btnMenos;
    private Botao btnAbrirLoja, btnSairLoja; // <--- NOVOS BOTÕES DA LOJA
    private float timerCentroX, timerCentroY, raioExternoTimer, raioInternoTimer;
    private float controlX, controlY, controlWidth, controlHeight, raioControle;

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        
        fonteTimer = new BitmapFont();
        fonteTimer.getData().setScale(3f); 
        fonteTimer.setColor(Color.BLACK);

        fonteBotao = new BitmapFont();
        fonteBotao.getData().setScale(1.2f);
        fonteBotao.setColor(Color.BLACK);

        banco = new GerenciadorMoedas();
        loja = new Loja();
        inventario = new Inventario(); 

        Color rosa = Color.valueOf("#E58F8F");
        btnEsq = new Botao("INICIAR", 140, 45, rosa);
        btnDir = new Botao("EDITAR", 140, 45, rosa);
        btnCentro = new Botao("ACEITAR", 140, 45, rosa);
        btnMais = new Botao("+", 40, 40, rosa);
        btnMenos = new Botao("-", 40, 40, rosa);
        
        // Inicializa os botões novos
        btnAbrirLoja = new Botao("LOJA", 140, 45, rosa);
        btnSairLoja = new Botao("SAIR", 140, 45, rosa);
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

        banco.calcularLayout(telaWidth, telaHeight);
        inventario.calcularLayout(telaWidth, telaHeight);

        controlWidth = telaWidth * 0.40f; 
        controlHeight = telaHeight * 0.9f;
        controlX = telaWidth * 0.05f;
        controlY = (telaHeight - controlHeight) / 2f;
        raioControle = bordaCm * 2f;

        loja.calcularLayout(controlX, controlY, controlWidth, controlHeight);

        timerCentroX = controlX + (controlWidth / 2f);
        timerCentroY = controlY + controlHeight - 220f; 
        raioExternoTimer = 110f;
        raioInternoTimer = 85f;

        // Posição dos Botões do Timer
        float btnY = timerCentroY - raioExternoTimer - 70f; 
        float espacamento = 20f;
        btnEsq.setPosicao(timerCentroX - btnEsq.width - (espacamento / 2f), btnY);
        btnDir.setPosicao(timerCentroX + (espacamento / 2f), btnY);
        btnCentro.setPosicao(timerCentroX - (btnCentro.width / 2f), btnY);
        
        float btnSinalY = timerCentroY - (btnMais.height / 2f);
        btnMenos.setPosicao(timerCentroX - raioExternoTimer - btnMenos.width - 20f, btnSinalY);
        btnMais.setPosicao(timerCentroX + raioExternoTimer + 20f, btnSinalY);

        // Posição do botão de ABRIR a loja (Fica abaixo do Iniciar/Editar)
        btnAbrirLoja.setPosicao(timerCentroX - (btnAbrirLoja.width / 2f), btnY - 70f);

        // Posição do botão de SAIR da loja (Fica na base do contêiner)
        btnSairLoja.setPosicao(timerCentroX - (btnSairLoja.width / 2f), controlY + 40f);
    }

    private void atualizarLogica() {
        // O Timer continua rodando mesmo se o jogador estiver vendo a loja
        if (estadoAtual == Estado.RODANDO && tempoAtual > 0) {
            tempoAtual -= Gdx.graphics.getDeltaTime();
            if (tempoAtual <= 0) {
                tempoAtual = 0;
                estadoAtual = Estado.PADRAO; 
                banco.adicionarMoedas((int) (tempoConfigurado / 60)); 
            }
        }

        boolean clicou = Gdx.input.justTouched();
        if (clicou) {
            float mouseX = Gdx.input.getX();
            float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();

            inventario.atualizarLogica(mouseX, mouseY, clicou);

            // ==========================================
            // LÓGICA DE CLIQUES BASEADA NA VIEW ATUAL
            // ==========================================
            if (viewAtual == ModoView.TIMER) {
                // SÓ PODE ABRIR A LOJA SE ESTIVER NO ESTADO PADRÃO (Parado)
                if (estadoAtual == Estado.PADRAO && btnAbrirLoja.isClicado(mouseX, mouseY)) {
                    viewAtual = ModoView.LOJA;
                }

                switch (estadoAtual) {
                    case PADRAO:
                        if (btnEsq.isClicado(mouseX, mouseY)) estadoAtual = Estado.RODANDO; 
                        else if (btnDir.isClicado(mouseX, mouseY)) estadoAtual = Estado.EDITANDO; 
                        break;
                    case EDITANDO:
                        if (btnCentro.isClicado(mouseX, mouseY)) estadoAtual = Estado.PADRAO; 
                        else if (btnMenos.isClicado(mouseX, mouseY) && tempoConfigurado > 300) { tempoConfigurado -= 300; tempoAtual = tempoConfigurado; }
                        else if (btnMais.isClicado(mouseX, mouseY) && tempoConfigurado < 5700) { tempoConfigurado += 300; tempoAtual = tempoConfigurado; }
                        break;
                    case RODANDO:
                    case PAUSADO:
                        if (btnEsq.isClicado(mouseX, mouseY)) estadoAtual = (estadoAtual == Estado.RODANDO) ? Estado.PAUSADO : Estado.RODANDO; 
                        else if (btnDir.isClicado(mouseX, mouseY)) { estadoAtual = Estado.PADRAO; tempoAtual = tempoConfigurado; }
                        break;
                }

            } else if (viewAtual == ModoView.LOJA) {
                // TELA DA LOJA
                if (btnSairLoja.isClicado(mouseX, mouseY)) {
                    viewAtual = ModoView.TIMER; // Volta pro cronômetro
                } else {
                    // Tenta clicar em um móvel da grade
                    Movel movelClicado = loja.getMovelClicado(mouseX, mouseY);
                    if (movelClicado != null) {
                        if (banco.gastarMoedas(movelClicado.preco)) {
                            loja.removerMovel(movelClicado); 
                            inventario.adicionarMovel(movelClicado); 
                        }
                    }
                }
            }
        }

        // Atualiza os textos dos botões do timer
        if (estadoAtual == Estado.RODANDO || estadoAtual == Estado.PAUSADO) {
            btnEsq.texto = (estadoAtual == Estado.RODANDO) ? "PAUSAR" : "RETOMAR";
            btnDir.texto = "CANCELAR";
        } else {
            btnEsq.texto = "INICIAR";
            btnDir.texto = "EDITAR";
        }
    }

    private void desenharTela() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // 1. DESENHA FUNDO DO CONTÊINER (Sempre Visível)
        shapeRenderer.setColor(Color.valueOf("#EDE8D8"));
        desenharRetangulo(controlX, controlY, controlWidth, controlHeight, raioControle);
        shapeRenderer.setColor(Color.valueOf("#EDE8D8"));
        float borda = Gdx.graphics.getPpcX();
        desenharRetangulo(controlX + borda, controlY + borda, controlWidth - (borda * 2), controlHeight - (borda * 2), Math.max(0, raioControle - borda));

        // 2. DESENHA CONTEÚDO BASEADO NA VIEW ATUAL
        if (viewAtual == ModoView.TIMER) {
            // Desenha Timer e Botões
            shapeRenderer.setColor(Color.valueOf("#D3D3D3"));
            shapeRenderer.circle(timerCentroX, timerCentroY, raioExternoTimer);
            float proporcao = tempoAtual / tempoConfigurado;
            if (proporcao > 0) {
                shapeRenderer.setColor(Color.valueOf("#696969"));
                shapeRenderer.arc(timerCentroX, timerCentroY, raioExternoTimer, 90, -(proporcao * 360f), 100);
            }
            shapeRenderer.setColor(Color.valueOf("#EDE8D8"));
            shapeRenderer.circle(timerCentroX, timerCentroY, raioInternoTimer);

            if (estadoAtual == Estado.PADRAO) {
                btnEsq.desenharShape(shapeRenderer);
                btnDir.desenharShape(shapeRenderer);
                btnAbrirLoja.desenharShape(shapeRenderer); // Botão de abrir loja
            } else if (estadoAtual == Estado.EDITANDO) {
                btnCentro.desenharShape(shapeRenderer);
                btnMenos.desenharShape(shapeRenderer);
                btnMais.desenharShape(shapeRenderer);
            } else {
                btnEsq.desenharShape(shapeRenderer);
                btnDir.desenharShape(shapeRenderer);
            }
        } else if (viewAtual == ModoView.LOJA) {
            // Desenha Grid da Loja e botão Sair
            loja.desenharShape(shapeRenderer);
            btnSairLoja.desenharShape(shapeRenderer);
        }

        banco.desenharShape(shapeRenderer); 
        shapeRenderer.end();

        // 3. DESENHA OS TEXTOS BASEADOS NA VIEW ATUAL
        batch.begin();
        banco.desenharTexto(batch, fonteBotao); 

        if (viewAtual == ModoView.TIMER) {
            int minutos = (int) (tempoAtual / 60);
            int segundos = (int) (tempoAtual % 60);
            String textoTimer = String.format("%02d:%02d", minutos, segundos);
            GlyphLayout layoutTimer = new GlyphLayout(fonteTimer, textoTimer);
            fonteTimer.draw(batch, layoutTimer, timerCentroX - (layoutTimer.width / 2f), timerCentroY + (layoutTimer.height / 2f));

            if (estadoAtual == Estado.PADRAO) {
                btnEsq.desenharTexto(batch, fonteBotao);
                btnDir.desenharTexto(batch, fonteBotao);
                btnAbrirLoja.desenharTexto(batch, fonteBotao);
            } else if (estadoAtual == Estado.EDITANDO) {
                btnCentro.desenharTexto(batch, fonteBotao);
                btnMenos.desenharTexto(batch, fonteBotao);
                btnMais.desenharTexto(batch, fonteBotao);
            } else {
                btnEsq.desenharTexto(batch, fonteBotao);
                btnDir.desenharTexto(batch, fonteBotao);
            }
        } else if (viewAtual == ModoView.LOJA) {
            // A loja agora desenha seus próprios textos (Nomes e Preços)
            loja.desenharTexto(batch, fonteBotao, banco.getSaldo());
            btnSairLoja.desenharTexto(batch, fonteBotao);
        }
        batch.end();

        inventario.desenhar(shapeRenderer, batch, fonteBotao);
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
        batch.dispose(); shapeRenderer.dispose();
        fonteTimer.dispose(); fonteBotao.dispose();
    }
}