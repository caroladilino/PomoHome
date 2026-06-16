package io.github.PomoHome.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import io.github.PomoHome.Main;
import io.github.PomoHome.model.Casa;
import io.github.PomoHome.model.Jogador;
import io.github.PomoHome.model.Jogo;
import io.github.PomoHome.model.Movel;
import io.github.PomoHome.model.timer.ContextoTimer;
import io.github.PomoHome.model.timer.EstadoTimer;
import io.github.PomoHome.network.ApiClient;
import io.github.PomoHome.ui.actors.CasaActor;
import io.github.PomoHome.ui.actors.CeuActor;
import io.github.PomoHome.ui.actors.CursorMovelActor;
import io.github.PomoHome.ui.actors.PainelActor;
import io.github.PomoHome.ui.actors.TimerRingActor;
import io.github.PomoHome.ui.comandos.ComandoColocar;
import io.github.PomoHome.ui.comandos.ComandoRemover;
import io.github.PomoHome.ui.comandos.GerenciadorComandos;
import io.github.PomoHome.ui.Palette;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The main game screen, fully scene2d. Everything draws through a single
 * {@link Stage} (sharing one {@link ExtendViewport}): the rounded pink panel and
 * the held-item cursor are {@link space.earlygrey.shapedrawer.ShapeDrawer} actors,
 * the Pomodoro ring is a {@link TimerRingActor}, the 8×8 isometric house is a
 * {@link CasaActor}, and the menu buttons / coin / titles / feedback are
 * {@link Label}s and {@link TextButton}s. No {@code ShapeRenderer}/{@code
 * SpriteBatch} or manual camera projection remains.
 *
 * <p>The 8×8 isometric house sits to the right of the panel and stays visible at
 * all times; the store and inventory render inside the left panel, never over
 * the house. The coin balance lives in the panel header.
 *
 * <p>Backend sync: loja catalog from {@code GET /loja}; purchases via
 * {@code comprarMovel}; a completed cycle POSTs {@code registrarSessao} (coins /
 * study time re-read from the server). House layout is persisted when edit mode
 * ends ({@code PUT /casas/{id}/layout}).
 *
 * <p>Edit mode: the "EDITAR CASA" button opens the inventory ({@code INVENTARIO}
 * panel mode); the timer can't run while editing. Closing it ("FECHAR") saves
 * the layout.
 *
 * <p>Input is an {@link InputMultiplexer}: the {@code Stage} gets first crack
 * (menu buttons, name field), then {@link #jogoInput} handles clicks that fall
 * through to the house grid (selection / placement).
 */
public class TelaJogo implements Screen {

    private enum ModoPainel { TIMER, LOJA, INVENTARIO }

    private final Main main;
    private final ApiClient api;
    private final Jogo jogo;
    private final ContextoTimer ctx;                       // GoF State + Observer
    private final GerenciadorComandos comandos = new GerenciadorComandos(); // GoF Command

    private BitmapFont fonteTimer, fonteBotao;

    // Fixed virtual resolution: the world stays 1280×720 world-units so the house
    // never distorts; ExtendViewport extends the shorter axis instead of
    // letterboxing. Layout math is in world units; mouse input is unprojected.
    private static final float MUNDO_W = 1280f;
    private static final float MUNDO_H = 720f;
    private OrthographicCamera camera;
    private ExtendViewport viewport;
    private final Vector2 toque = new Vector2();

    private Stage stage;
    private ShapeDrawer drawer;        // draws shapes through the Stage's batch
    private CeuActor ceuActor;         // sun/moon arcing in the sky (behind all)
    private PainelActor painel;
    private TimerRingActor timerRing;
    private TextButton btnEsq, btnDir, btnCentro, btnMais, btnMenos;
    private TextButton btnLoja, btnEditarCasa, btnRanking, btnAmigos, btnHistorico, btnFechar;
    private Label lblMoeda, lblTituloPainel, lblFeedback;

    // Store + inventory: a ScrollPane of a 2-column Table each, rebuilt on demand.
    private ScrollPane lojaScroll, invScroll;
    private Table lojaTabela, invTabela;

    // House grid actor + edit-mode chrome (name field, "X" remove over the
    // selected tile).
    private CasaActor casaActor;
    private TextField campoNome;
    private TextButton btnRemover, btnDesfazer, btnRefazer;
    private boolean temSelecao = false;
    private final Vector2 tmp = new Vector2();

    private ModoPainel viewAtual = ModoPainel.TIMER;
    private Movel movelNaMao = null;

    private float controlX, controlY, controlWidth, controlHeight;
    private float timerCentroX, timerCentroY, raioExternoTimer, raioInternoTimer;

    private String feedback = "";
    private float feedbackTempo = 0f;

    public TelaJogo(Main main) {
        this.main = main;
        this.api = main.getApiClient();
        this.jogo = main.getJogo();
        this.ctx = jogo.getContextoTimer();

        camera = new OrthographicCamera();
        viewport = new ExtendViewport(MUNDO_W, MUNDO_H, camera);

        // Crisp FreeType fonts shared from the Skin (built once in Main). Drawn
        // at their native size — no getData().setScale() blur. The Skin owns
        // them, so we never dispose them here.
        fonteTimer = main.getSkin().getFont("timer");      // 48px bold, timer digits
        fonteBotao = main.getSkin().getFont("default-font"); // 18px body text

        construirUi();

        // GoF Observer: react to cycle completion without the timer knowing us.
        // Registered once here (the screen is cached/reused by Main, so doing this
        // in show() would subscribe again on every return to the game).
        ctx.adicionarOuvinte(this::onPomodoroCompleto);
    }

    // ---------------------------------------------------------------
    // scene2d UI
    // ---------------------------------------------------------------

    private void construirUi() {
        // PolygonSpriteBatch so ShapeDrawer's filled shapes render efficiently;
        // the Stage owns + disposes it. The ShapeDrawer (panel, ring, house grid,
        // cursor) draws through that batch using the skin's 1×1 white region.
        stage = new Stage(viewport, new PolygonSpriteBatch());
        drawer = new ShapeDrawer(stage.getBatch(), main.getSkin().getRegion("white"));

        // Background actors, bottom to top: sky (sun/moon), panel, timer ring, house.
        ceuActor = new CeuActor(jogo.getCicloDiaNoite(), drawer);
        painel = new PainelActor(drawer);
        timerRing = new TimerRingActor(jogo.getTimer(), drawer, fonteTimer);
        casaActor = new CasaActor(casaDoJogador(), true, drawer, fonteBotao);
        stage.addActor(ceuActor);
        stage.addActor(painel);
        stage.addActor(timerRing);
        stage.addActor(casaActor);

        criarMenu();
        criarPaineis();
        criarChromeEdicao();
        stage.addActor(lblTituloPainel);
        stage.addActor(lblFeedback);

        // Held item follows the cursor — topmost overlay.
        stage.addActor(new CursorMovelActor(() -> movelNaMao, drawer, viewport));
    }

    /** Timer/navigation buttons, the coin/title/feedback labels, and their actions. */
    private void criarMenu() {
        btnEsq = botaoRosa("INICIAR");
        btnDir = botaoRosa("EDITAR");
        btnCentro = botaoRosa("ACEITAR");
        btnMais = botaoRosa("+");
        btnMenos = botaoRosa("-");
        btnLoja = botaoRosa("LOJA");
        btnEditarCasa = botaoRosa("EDITAR CASA");
        btnRanking = botaoRosa("RANKING");
        btnAmigos = botaoRosa("AMIGOS");
        btnHistorico = botaoRosa("HISTÓRICO");

        lblMoeda = new Label("$0", main.getSkin());
        lblMoeda.setColor(Palette.OURO_TEXTO); // deep gold, reads on parchment
        lblTituloPainel = new Label("", main.getSkin());
        lblTituloPainel.setColor(Palette.TEXTO_ESCURO);
        lblFeedback = new Label("", main.getSkin());
        lblFeedback.setColor(Palette.TEXTO_CLARO);

        aoClicar(btnEsq, ctx::iniciarOuPausar);
        aoClicar(btnDir, ctx::editarOuCancelar);
        aoClicar(btnCentro, ctx::aceitar);
        aoClicar(btnMais, () -> jogo.getTimer().aumentarCiclo());
        aoClicar(btnMenos, () -> jogo.getTimer().diminuirCiclo());
        aoClicar(btnLoja, this::abrirLoja);
        aoClicar(btnEditarCasa, this::abrirInventario);
        aoClicar(btnRanking, () -> main.setScreen(new TelaRanking(main)));
        aoClicar(btnAmigos, () -> main.setScreen(new TelaAmigos(main)));
        aoClicar(btnHistorico, () -> main.setScreen(new TelaHistorico(main)));

        for (TextButton b : new TextButton[]{btnEsq, btnDir, btnCentro, btnMais, btnMenos,
                btnLoja, btnEditarCasa, btnRanking, btnAmigos, btnHistorico}) {
            stage.addActor(b);
        }
        stage.addActor(lblMoeda);
    }

    /** Store + inventory scroll panes and the shared FECHAR button. */
    private void criarPaineis() {
        lojaTabela = new Table().top();
        lojaScroll = scrollVertical(lojaTabela);
        invTabela = new Table().top();
        invScroll = scrollVertical(invTabela);

        btnFechar = botaoRosa("FECHAR");
        aoClicar(btnFechar, () -> {
            if (viewAtual == ModoPainel.LOJA) {
                viewAtual = ModoPainel.TIMER;
            } else if (viewAtual == ModoPainel.INVENTARIO) {
                fecharInventario(jogo.getJogadorLogado());
            }
        });

        stage.addActor(lojaScroll);
        stage.addActor(invScroll);
        stage.addActor(btnFechar);
    }

    /** House-name field (editable only in edit mode) and the "X" remove button. */
    private void criarChromeEdicao() {
        campoNome = new TextField("", main.getSkin());
        campoNome.setAlignment(com.badlogic.gdx.utils.Align.center);

        btnRemover = botaoRosa("X");
        aoClicar(btnRemover, () -> {
            // GoF Command: removal goes through the manager so it can be undone.
            if (comandos.executar(new ComandoRemover(casaActor))) {
                reconstruirInventario(); // the removed móvel returns to stock
            }
        });

        // Undo / redo of house edits (GoF Command).
        btnDesfazer = botaoRosa("DESFAZER");
        aoClicar(btnDesfazer, () -> {
            comandos.desfazer();
            casaActor.limparSelecao();
            reconstruirInventario();
        });
        btnRefazer = botaoRosa("REFAZER");
        aoClicar(btnRefazer, () -> {
            comandos.refazer();
            casaActor.limparSelecao();
            reconstruirInventario();
        });

        stage.addActor(campoNome);
        stage.addActor(btnRemover);
        stage.addActor(btnDesfazer);
        stage.addActor(btnRefazer);
    }

    private TextButton botaoRosa(String texto) {
        return new TextButton(texto, main.getSkin(), "rosa");
    }

    /** A vertical-only ScrollPane that never steals its cells' clicks (no drag-scroll). */
    private ScrollPane scrollVertical(Table conteudo) {
        // "painel" style: a visible taupe/sage scrollbar tuned for the cream panel.
        ScrollPane sp = new ScrollPane(conteudo, main.getSkin(), "painel");
        sp.setFadeScrollBars(false);
        sp.setScrollingDisabled(true, false);
        sp.setFlickScroll(false);
        sp.setOverscroll(false, false);
        sp.setCancelTouchFocus(false);
        return sp;
    }

    private Casa casaDoJogador() {
        Jogador jogador = jogo.getJogadorLogado();
        return jogador != null ? jogador.getCasa() : null;
    }

    // ---------------------------------------------------------------
    // Store + inventory tables (scene2d)
    // ---------------------------------------------------------------

    /**
     * Rebuilds the store grid from the catalog, hiding móveis the player already
     * owns (each catalog item is buyable at most once). Prices red if unaffordable.
     */
    private void reconstruirLoja() {
        lojaTabela.clearChildren();
        Jogador jogador = jogo.getJogadorLogado();
        int saldo = jogador != null ? jogador.getSaldo() : 0;
        List<Movel> itens = jogo.getLoja().getItensDisponiveis();

        Set<Long> possuidos = new HashSet<>();
        if (jogador != null && jogador.getInventario() != null) {
            for (Movel m : jogador.getInventario()) {
                if (m.getId() != null) {
                    possuidos.add(m.getId());
                }
            }
        }

        int col = 0;
        if (itens != null) {
            for (Movel m : itens) {
                if (m.getId() != null && possuidos.contains(m.getId())) {
                    continue; // already owned — not offered again
                }
                boolean podePagar = m.getPreco() <= saldo;
                Table cell = criarCelula(m.getNome(), "$" + m.getPreco(),
                        podePagar ? Palette.TEXTO_ESCURO : Palette.ERRO, Palette.CELULA_LOJA,
                        () -> { if (jogador != null) comprar(jogador, m); });
                lojaTabela.add(cell).size(110f, 84f).pad(8f);
                if (++col % 2 == 0) {
                    lojaTabela.row();
                }
            }
        }
        if (col == 0) {
            lojaTabela.add(new Label("Loja vazia", main.getSkin())).pad(10f);
        }
    }

    /** Rebuilds the inventory grid from "owned − placed − in-hand". */
    private void reconstruirInventario() {
        invTabela.clearChildren();
        Jogador jogador = jogo.getJogadorLogado();
        List<Movel> itens = jogador != null ? inventarioExibido(jogador) : null;
        if (itens == null || itens.isEmpty()) {
            invTabela.add(new Label("Inventário vazio", main.getSkin())).pad(10f);
            return;
        }
        int col = 0;
        for (Movel m : itens) {
            Table cell = criarCelula(m.getNome(), null, Palette.TEXTO_ESCURO, Palette.CELULA_INV,
                    () -> {
                        movelNaMao = m;          // pick up (drop on the grid next)
                        casaActor.limparSelecao();
                        reconstruirInventario();
                    });
            invTabela.add(cell).size(96f, 74f).pad(8f);
            if (++col % 2 == 0) {
                invTabela.row();
            }
        }
    }

    /** One clickable item cell: a tinted box with a name and (optional) price label. */
    private Table criarCelula(String nome, String preco, Color corPreco, Color corFundo,
                              Runnable aoClicar) {
        Table cell = new Table();
        cell.setBackground(main.getSkin().newDrawable("white", corFundo));
        cell.setTouchable(Touchable.enabled);

        // Name: centred and clipped with an ellipsis so a long name never spills
        // outside the box. growX pins it to the (fixed) cell width so the ellipsis
        // actually engages instead of widening the cell.
        Label lNome = new Label(nome, main.getSkin());
        lNome.setColor(Palette.TEXTO_ESCURO);
        lNome.setEllipsis(true);
        lNome.setAlignment(com.badlogic.gdx.utils.Align.center);
        lNome.setTouchable(Touchable.disabled);
        cell.add(lNome).growX().expandY().center().padLeft(6f).padRight(6f).row();

        if (preco != null) {
            Label lPreco = new Label(preco, main.getSkin());
            lPreco.setColor(corPreco);
            lPreco.setTouchable(Touchable.disabled);
            cell.add(lPreco).padBottom(6f);
        }

        // The labels are non-touchable, so the cell itself is the hit target across
        // its whole area — a click anywhere in the box counts, not just on the text.
        cell.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                aoClicar.run();
            }
        });
        return cell;
    }

    private void aoClicar(Button botao, Runnable acao) {
        botao.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                acao.run();
            }
        });
    }

    @Override
    public void show() {
        // The window may have been resized while another screen was showing.
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        casaActor.reconstruirDoModelo();
        // Refresh the catalog, then (re)build the store grid once it arrives.
        jogo.getLoja().atualizarDoServidor(api, this::reconstruirLoja);
        reconstruirLoja();
        reconstruirInventario();

        // Stage first (menu buttons, name field, remove button), then the click
        // handler for grid placement / inventory-drop that falls through it.
        InputMultiplexer mux = new InputMultiplexer(stage, jogoInput);
        Gdx.input.setInputProcessor(mux);
    }

    @Override
    public void render(float delta) {
        calcularLayout();
        atualizarLogica(delta);
        atualizarBotoes();

        // RNF02: the world advances day↔night only while this (the main house)
        // screen is open; other screens just read the resulting phase.
        jogo.getCicloDiaNoite().avancar(delta);

        ScreenUtils.clear(Palette.ceu(jogo.getCicloDiaNoite().fatorNoite()));
        stage.act(delta);
        stage.draw();
    }

    // ---------------------------------------------------------------
    // Layout
    // ---------------------------------------------------------------

    private void calcularLayout() {
        float w = viewport.getWorldWidth();
        float h = viewport.getWorldHeight();
        ceuActor.setBounds(0f, 0f, w, h);   // sky spans the whole screen
        layoutPainel(w, h);
        layoutCasa(w, h);
        layoutMenu();
    }

    /** Left panel: its bounds, the store/inventory scroll area, and the labels. */
    private void layoutPainel(float w, float h) {
        controlWidth = w * 0.40f;
        controlHeight = h * 0.9f;
        controlX = w * 0.05f;
        controlY = (h - controlHeight) / 2f;
        painel.setBounds(controlX, controlY, controlWidth, controlHeight);

        float borda = PainelActor.BORDA;
        float scrollX = controlX + borda;
        float scrollTop = controlY + controlHeight - borda - 80f;
        float scrollBottom = controlY + 40f + 44f + 12f; // above the FECHAR button
        float scrollW = controlWidth - borda * 2f;
        lojaScroll.setBounds(scrollX, scrollBottom, scrollW, scrollTop - scrollBottom);
        invScroll.setBounds(scrollX, scrollBottom, scrollW, scrollTop - scrollBottom);

        lblMoeda.setPosition(controlX + borda + 16f, controlY + controlHeight - borda - 30f);
        lblTituloPainel.setPosition(controlX + borda + 16f, controlY + controlHeight - borda - 72f);
        lblFeedback.setPosition(controlX + controlWidth + 30f, controlY + controlHeight - 32f);
    }

    /** House grid (right of the panel) plus the name field and "X" remove button. */
    private void layoutCasa(float w, float h) {
        float areaX = w * 0.45f + 30f;
        float areaBaixo = 90f;
        float areaTopo = h - 60f;
        casaActor.setBounds(areaX, areaBaixo, w - areaX - 40f, areaTopo - areaBaixo);

        // Both anchored to the actor's previous-frame geometry — stable after the
        // first frame and after any resize.
        casaActor.ancoraNome(tmp);
        campoNome.setBounds(tmp.x - 110f, tmp.y - 36f, 220f, 36f);
        // Undo/redo a row below the name field (still anchored to the grid).
        float btW = 104f, btH = 36f, gap = 12f, btY = tmp.y - 36f - 44f;
        btnDesfazer.setBounds(tmp.x - btW - gap / 2f, btY, btW, btH);
        btnRefazer.setBounds(tmp.x + gap / 2f, btY, btW, btH);

        temSelecao = casaActor.centroSelecionado(tmp);
        if (temSelecao) {
            btnRemover.setBounds(tmp.x - 18f, tmp.y + 16f, 36f, 36f);
        }
    }

    /** Timer ring and the button grid below it (three rows of two + the ± and FECHAR). */
    private void layoutMenu() {
        timerCentroX = controlX + controlWidth / 2f;
        timerCentroY = controlY + controlHeight - 200f;
        raioExternoTimer = 110f;
        raioInternoTimer = 85f;
        timerRing.setRaios(raioExternoTimer, raioInternoTimer);
        timerRing.setBounds(timerCentroX - raioExternoTimer, timerCentroY - raioExternoTimer,
                raioExternoTimer * 2f, raioExternoTimer * 2f);

        float bW = 150f, bH = 44f, gap = 16f, rowStep = 56f;
        float colL = timerCentroX - gap / 2f - bW;
        float colR = timerCentroX + gap / 2f;
        float rowTop = timerCentroY - raioExternoTimer - 55f;

        btnEsq.setBounds(colL, rowTop, bW, bH);
        btnDir.setBounds(colR, rowTop, bW, bH);
        btnLoja.setBounds(colL, rowTop - rowStep, bW, bH);
        btnEditarCasa.setBounds(colR, rowTop - rowStep, bW, bH);
        btnRanking.setBounds(colL, rowTop - 2f * rowStep, bW, bH);
        btnAmigos.setBounds(colR, rowTop - 2f * rowStep, bW, bH);
        btnHistorico.setBounds(timerCentroX - bW / 2f, rowTop - 3f * rowStep, bW, bH);

        // ACEITAR under the ring, ± on its sides.
        btnCentro.setBounds(timerCentroX - bW / 2f, rowTop, bW, bH);
        btnMenos.setBounds(timerCentroX - raioExternoTimer - 60f, timerCentroY - 20f, 40f, 40f);
        btnMais.setBounds(timerCentroX + raioExternoTimer + 20f, timerCentroY - 20f, 40f, 40f);

        btnFechar.setBounds(timerCentroX - bW / 2f, controlY + 40f, bW, bH);
    }

    // ---------------------------------------------------------------
    // Logic
    // ---------------------------------------------------------------

    private void atualizarLogica(float delta) {
        if (jogo.getJogadorLogado() == null) {
            return;
        }
        if (feedbackTempo > 0) {
            feedbackTempo -= delta;
        }
        // GoF State: the current state advances the countdown and, on completion,
        // transitions to idle and notifies the observers (see onPomodoroCompleto).
        ctx.atualizar(delta);
    }

    /** Updates the menu's per-state button visibility / labels + the coin count. */
    private void atualizarBotoes() {
        boolean timerMode = viewAtual == ModoPainel.TIMER;
        // GoF State: the current state decides which buttons show and their labels.
        EstadoTimer estado = ctx.getEstado();
        boolean iniciarEditar = timerMode && estado.mostraIniciarEditar();
        boolean navegacao = timerMode && estado.mostraNavegacao();
        boolean controlesEdicao = timerMode && estado.mostraControlesEdicao();

        btnEsq.setVisible(iniciarEditar);
        btnDir.setVisible(iniciarEditar);
        btnEsq.setText(estado.textoBotaoEsquerdo());
        btnDir.setText(estado.textoBotaoDireito());

        btnLoja.setVisible(navegacao);
        btnEditarCasa.setVisible(navegacao);
        btnRanking.setVisible(navegacao);
        btnAmigos.setVisible(navegacao);
        btnHistorico.setVisible(navegacao);

        btnCentro.setVisible(controlesEdicao);
        btnMais.setVisible(controlesEdicao);
        btnMenos.setVisible(controlesEdicao);

        btnFechar.setVisible(!timerMode);
        btnFechar.setText(viewAtual == ModoPainel.LOJA ? "SAIR" : "FECHAR");

        timerRing.setVisible(viewAtual == ModoPainel.TIMER);
        lojaScroll.setVisible(viewAtual == ModoPainel.LOJA);
        invScroll.setVisible(viewAtual == ModoPainel.INVENTARIO);
        // While an item is "in hand", disable the inventory's touch so clicks
        // fall through to the grid (place) or panel (drop back to inventory).
        invScroll.setTouchable(movelNaMao == null ? Touchable.enabled : Touchable.disabled);

        Jogador jogador = jogo.getJogadorLogado();
        Casa casa = jogador != null ? jogador.getCasa() : null;

        // House name: editable only in edit mode; otherwise show it disabled and
        // keep it in sync with the model (a rename elsewhere still reflects here).
        boolean editandoCasa = viewAtual == ModoPainel.INVENTARIO;
        campoNome.setDisabled(!editandoCasa);
        if (!editandoCasa && casa != null) {
            campoNome.setText(casa.getNome() != null ? casa.getNome() : "");
        }
        // The "X" only shows in edit mode when a placed móvel is selected.
        btnRemover.setVisible(editandoCasa && temSelecao);
        // Undo/redo show throughout edit mode, disabled at the ends of history.
        btnDesfazer.setVisible(editandoCasa);
        btnRefazer.setVisible(editandoCasa);
        btnDesfazer.setDisabled(!comandos.podeDesfazer());
        btnRefazer.setDisabled(!comandos.podeRefazer());

        lblMoeda.setText("$" + (jogador != null ? jogador.getSaldo() : 0));

        // Panel title shows only over the store / inventory.
        lblTituloPainel.setVisible(!timerMode);
        if (!timerMode) {
            lblTituloPainel.setText(viewAtual == ModoPainel.LOJA
                    ? "Loja" : "Inventário (clique p/ pegar)");
        }

        // Transient feedback message fades after a couple of seconds.
        lblFeedback.setText(feedback);
        lblFeedback.setVisible(feedbackTempo > 0f);
    }

    /**
     * Clicks that fall through the {@link Stage} (i.e. not on a menu button or
     * the scene2d store/inventory grids): house-grid placement, selection and
     * dropping a held item. Only active in INVENTARIO (edit mode) — store buying
     * and inventory picking are handled by the scene2d cells themselves.
     */
    private final InputAdapter jogoInput = new InputAdapter() {
        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            Jogador jogador = jogo.getJogadorLogado();
            if (jogador == null || viewAtual != ModoPainel.INVENTARIO) {
                return false;
            }
            toque.set(screenX, screenY);
            viewport.unproject(toque);
            tratarCliqueInventario(toque.x, toque.y, jogador);
            return true;
        }
    };

    private void tratarCliqueInventario(float mx, float my, Jogador jogador) {
        if (movelNaMao == null) {
            // Picking is done by the scene2d inventory cells, removal by the "X"
            // button; here a bare grid click just selects/deselects a placed móvel.
            casaActor.selecionarSob(mx, my);
        } else if (cliqueNoPainel(mx, my)) {
            movelNaMao = null;        // dropped back into the inventory
            reconstruirInventario();
        } else if (comandos.executar(new ComandoColocar(casaActor, movelNaMao, mx, my))) {
            movelNaMao = null;        // placed on the grid (GoF Command — undoable)
            reconstruirInventario();
        }
    }

    private boolean cliqueNoPainel(float mx, float my) {
        return mx >= controlX && mx <= controlX + controlWidth
                && my >= controlY && my <= controlY + controlHeight;
    }

    private void abrirLoja() {
        viewAtual = ModoPainel.LOJA;
        reconstruirLoja();
    }

    private void abrirInventario() {
        viewAtual = ModoPainel.INVENTARIO;
        movelNaMao = null;
        comandos.limpar(); // undo history is per edit session
        casaActor.limparSelecao();
        Casa casa = casaDoJogador();
        campoNome.setText(casa != null && casa.getNome() != null ? casa.getNome() : "");
        reconstruirInventario();
    }

    private void fecharInventario(Jogador jogador) {
        viewAtual = ModoPainel.TIMER;
        movelNaMao = null;
        if (stage != null) {
            stage.setKeyboardFocus(null);
        }
        if (jogador != null) {
            commitNomeCasa(jogador.getCasa());
            salvarLayout(jogador);
        }
    }

    /**
     * Validate the edited house name and commit it to the model (so the layout
     * save sends it). Invalid names are rejected with a brief message and the old
     * name is kept.
     */
    private void commitNomeCasa(Casa casa) {
        if (casa == null) {
            return;
        }
        String novo = campoNome.getText() != null ? campoNome.getText().trim() : "";
        if (novo.isEmpty() || !novo.matches("^[a-zA-Z0-9À-ÿ ]+$")) {
            campoNome.setText(casa.getNome() != null ? casa.getNome() : "");
            mostrarFeedback("Nome inválido!");
        } else {
            casa.setNome(novo);
        }
    }

    // ---------------------------------------------------------------
    // Backend sync
    // ---------------------------------------------------------------

    /**
     * GoF Observer callback (registered on {@link ContextoTimer} in the
     * constructor): a cycle just completed — credit the session on the server and
     * refresh coins / study time. The state machine already returned to idle.
     */
    private void onPomodoroCompleto(final int minutos) {
        final Jogador jogador = jogo.getJogadorLogado();
        if (jogador == null || jogador.getId() == null || minutos <= 0) {
            return;
        }
        api.registrarSessao(jogador.getId(), minutos, new ApiClient.Callback<io.github.PomoHome.model.SessaoEstudo>() {
            @Override
            public void onSuccess(io.github.PomoHome.model.SessaoEstudo result) {
                api.fetchJogadorPorId(jogador.getId(), new ApiClient.Callback<Jogador>() {
                    @Override
                    public void onSuccess(Jogador atualizado) {
                        Gdx.app.postRunnable(() -> {
                            jogador.setSaldo(atualizado.getSaldo());
                            jogador.setTempoEstudado(atualizado.getTempoEstudado());
                            mostrarFeedback("+" + minutos + " moedas");
                        });
                    }
                    @Override public void onError(Throwable t) {
                        Gdx.app.postRunnable(() -> mostrarFeedback("+" + minutos + " moedas"));
                    }
                });
            }
            @Override
            public void onError(Throwable t) {
                Gdx.app.postRunnable(() -> mostrarFeedback("Falha ao salvar sessão"));
            }
        });
    }

    private void comprar(Jogador jogador, Movel item) {
        if (jogador.getId() == null || item.getId() == null) {
            return;
        }
        api.comprarMovel(jogador.getId(), item.getId(), new ApiClient.Callback<Jogador>() {
            @Override
            public void onSuccess(Jogador atualizado) {
                Gdx.app.postRunnable(() -> {
                    jogador.setSaldo(atualizado.getSaldo());
                    jogador.setInventario(resolverInventario(atualizado.getInventario()));
                    reconstruirLoja();       // affordability (saldo) changed
                    reconstruirInventario(); // new item owned
                    mostrarFeedback(item.getNome() + " comprado!");
                });
            }
            @Override
            public void onError(Throwable t) {
                Gdx.app.postRunnable(() -> mostrarFeedback(t.getMessage()));
            }
        });
    }

    private void salvarLayout(Jogador jogador) {
        Casa casa = jogador.getCasa();
        if (casa == null || casa.getId() == null) {
            return;
        }
        api.salvarLayoutCasa(casa.getId(), casa.getNome(), casa.toPlacements(),
                new ApiClient.Callback<Casa>() {
            @Override public void onSuccess(Casa result) {
                Gdx.app.postRunnable(() -> mostrarFeedback("Casa salva"));
            }
            @Override public void onError(Throwable t) {
                Gdx.app.postRunnable(() -> mostrarFeedback("Falha ao salvar casa"));
            }
        });
    }

    /** Inventory shown = owned − (placed by id) − (the piece currently in hand). */
    private List<Movel> inventarioExibido(Jogador jogador) {
        List<Movel> owned = jogador.getInventario();
        List<Movel> out = new ArrayList<>();
        if (owned == null) {
            return out;
        }
        Map<Long, Integer> esconder = new HashMap<>();
        if (jogador.getCasa() != null) {
            for (Movel m : jogador.getCasa().getPlacements().values()) {
                if (m != null && m.getId() != null) {
                    esconder.merge(m.getId(), 1, Integer::sum);
                }
            }
        }
        if (movelNaMao != null && movelNaMao.getId() != null) {
            esconder.merge(movelNaMao.getId(), 1, Integer::sum);
        }
        for (Movel m : owned) {
            Long id = m.getId();
            if (id != null && esconder.getOrDefault(id, 0) > 0) {
                esconder.merge(id, -1, Integer::sum);
            } else {
                out.add(m);
            }
        }
        return out;
    }

    private static List<Movel> resolverInventario(List<Movel> inv) {
        if (inv != null) {
            for (Movel m : inv) {
                m.resolverTamanho();
            }
        }
        return inv;
    }

    private void mostrarFeedback(String texto) {
        feedback = texto == null ? "" : texto;
        feedbackTempo = 2.5f;
    }

    // ---------------------------------------------------------------
    // Screen lifecycle
    // ---------------------------------------------------------------

    @Override
    public void resize(int width, int height) {
        // The viewport keeps the world a fixed 1280×720 (so the house never
        // distorts) and recomputes the camera; the Stage shares this viewport and
        // applies it on draw.
        viewport.update(width, height, true);
    }

    @Override public void pause() { }
    @Override public void resume() { }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
        if (stage != null) {
            stage.setKeyboardFocus(null);
        }
    }

    @Override
    public void dispose() {
        // Idempotent: Main may dispose both the current screen and the cached
        // game-screen instance, so null everything out after freeing it. The Stage
        // owns its PolygonSpriteBatch (and disposes it); the ShapeDrawer holds no
        // resources of its own. The fonts/white region belong to the shared Skin.
        if (stage != null) { stage.dispose(); stage = null; }
        fonteTimer = null;
        fonteBotao = null;
    }
}
