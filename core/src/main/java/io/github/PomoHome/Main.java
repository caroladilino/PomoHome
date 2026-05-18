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

    private enum Estado { PADRAO, EDITANDO, RODANDO, PAUSADO }
    private Estado estadoAtual = Estado.PADRAO;
    
    private enum ModoView { TIMER, LOJA }
    private ModoView viewAtual = ModoView.TIMER;

    private float tempoConfigurado = 53 * 60; 
    private float tempoAtual = tempoConfigurado;

    private GerenciadorMoedas banco;
    private Loja loja; 
    private Inventario inventario;
    private Casa casa;

    private Movel movelNaMao = null; 

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

        banco = new GerenciadorMoedas();
        loja = new Loja();
        inventario = new Inventario(); 
        casa = new Casa(); 

        Color rosa = Color.valueOf("#E58F8F");
        btnEsq = new Botao("INICIAR", 140, 45, rosa);
        btnDir = new Botao("EDITAR", 140, 45, rosa);
        btnCentro = new Botao("ACEITAR", 140, 45, rosa);
        btnMais = new Botao("+", 40, 40, rosa);
        btnMenos = new Botao("-", 40, 40, rosa);
        
        btnAbrirLoja = new Botao("LOJA", 140, 45, rosa);
        btnSairLoja = new Botao("SAIR", 140, 45, rosa);

        Gdx.input.setInputProcessor(casa.inputNomeCasa);
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
        casa.calcularLayout(telaWidth, telaHeight); 

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

        float btnY = timerCentroY - raioExternoTimer - 70f; 
        float espacamento = 20f;
        btnEsq.setPosicao(timerCentroX - btnEsq.width - (espacamento / 2f), btnY);
        btnDir.setPosicao(timerCentroX + (espacamento / 2f), btnY);
        btnCentro.setPosicao(timerCentroX - (btnCentro.width / 2f), btnY);
        
        float btnSinalY = timerCentroY - (btnMais.height / 2f);
        btnMenos.setPosicao(timerCentroX - raioExternoTimer - btnMenos.width - 20f, btnSinalY);
        btnMais.setPosicao(timerCentroX + raioExternoTimer + 20f, btnSinalY);

        btnAbrirLoja.setPosicao(timerCentroX - (btnAbrirLoja.width / 2f), btnY - 70f);
        btnSairLoja.setPosicao(timerCentroX - (btnSairLoja.width / 2f), controlY + 40f);
    }

    private void atualizarLogica() {
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

            // --- FLUXO DE DECORAÇÃO DA CASA ---
            if (movelNaMao == null) {
                movelNaMao = inventario.pegarMovel(mouseX, mouseY);
                
                if (movelNaMao != null) {
                    casa.limparSelecao(); // Esconde o botão 'X' ao pegar um item
                } else {
                    // Mão continua vazia, passa o clique para a casa (Pode ser pra editar nome ou remover móvel)
                    casa.atualizarLogica(mouseX, mouseY, clicou, inventario);
                }
            } else {
                if (inventario.isClicadoNoPainel(mouseX, mouseY)) {
                    inventario.adicionarMovel(movelNaMao);
                    movelNaMao = null; 
                } 
                else if (casa.tentarColocarMovel(mouseX, mouseY, movelNaMao)) {
                    movelNaMao = null; 
                }
            }

            // --- LÓGICA DE INTERFACE PADRÃO ---
            inventario.atualizarLogica(mouseX, mouseY, clicou);

            if (movelNaMao == null) {
                if (viewAtual == ModoView.TIMER) {
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
                    if (btnSairLoja.isClicado(mouseX, mouseY)) {
                        viewAtual = ModoView.TIMER; 
                    } else {
                        Movel m = loja.getMovelClicado(mouseX, mouseY);
                        if (m != null && banco.gastarMoedas(m.preco)) {
                            loja.removerMovel(m); 
                            inventario.adicionarMovel(m); 
                        }
                    }
                }
            }
        }

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
        
        shapeRenderer.setColor(Color.PINK);
        desenharRetangulo(controlX, controlY, controlWidth, controlHeight, raioControle);
        shapeRenderer.setColor(Color.valueOf("#EDE8D8"));
        float borda = Gdx.graphics.getPpcX();
        desenharRetangulo(controlX + borda, controlY + borda, controlWidth - (borda * 2), controlHeight - (borda * 2), Math.max(0, raioControle - borda));

        casa.desenharShape(shapeRenderer);

        if (viewAtual == ModoView.TIMER) {
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
                btnAbrirLoja.desenharShape(shapeRenderer);
            } else if (estadoAtual == Estado.EDITANDO) {
                btnCentro.desenharShape(shapeRenderer);
                btnMenos.desenharShape(shapeRenderer);
                btnMais.desenharShape(shapeRenderer);
            } else {
                btnEsq.desenharShape(shapeRenderer);
                btnDir.desenharShape(shapeRenderer);
            }
        } else if (viewAtual == ModoView.LOJA) {
            loja.desenharShape(shapeRenderer);
            btnSairLoja.desenharShape(shapeRenderer);
        }

        banco.desenharShape(shapeRenderer); 

        if (movelNaMao != null) {
            float mouseX = Gdx.input.getX();
            float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
            shapeRenderer.setColor(movelNaMao.corRepresentativa);
            shapeRenderer.rect(mouseX - 30f, mouseY - 30f, 60f, 60f); 
        }

        shapeRenderer.end();

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
            loja.desenharTexto(batch, fonteBotao, banco.getSaldo());
            btnSairLoja.desenharTexto(batch, fonteBotao);
        }

        casa.desenharTexto(batch, fonteBotao);
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
        batch.dispose(); 
        shapeRenderer.dispose();
        fonteTimer.dispose(); 
        fonteBotao.dispose();
    }
}