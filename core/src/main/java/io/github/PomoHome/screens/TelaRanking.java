package io.github.PomoHome.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.PomoHome.Main;
import io.github.PomoHome.model.Jogador;
import io.github.PomoHome.ui.Palette;

import java.util.List;

/**
 * Ranking screen (scene2d) — all players sorted by tempoEstudado DESC, as
 * served by {@code GET /api/jogadores/ranking}. The logged-in player's row is
 * highlighted. "Voltar" returns to the cached game screen.
 */
public class TelaRanking implements Screen {

    private final Main main;
    private Stage stage;
    private Table listaTabela;
    private Label statusLabel;

    public TelaRanking(Main main) {
        this.main = main;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        Table root = new Table();
        root.setFillParent(true);
        root.pad(24f);
        stage.addActor(root);

        Label titulo = new Label("Ranking", main.getSkin());

        TextButton voltar = new TextButton("Voltar", main.getSkin());
        voltar.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                main.voltarAoJogo();
            }
        });

        statusLabel = new Label("Carregando...", main.getSkin());
        statusLabel.setColor(Palette.NEUTRO);

        listaTabela = new Table();
        ScrollPane scroll = new ScrollPane(listaTabela, main.getSkin());
        scroll.setFadeScrollBars(false);

        root.add(titulo).padBottom(16f).row();
        root.add(statusLabel).padBottom(10f).row();
        root.add(scroll).width(420f).height(360f).padBottom(16f).row();
        root.add(voltar).width(160f).row();

        stage.addListener(new InputListener() {
            @Override public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    main.voltarAoJogo();
                    return true;
                }
                return false;
            }
        });

        carregarRanking();
    }

    private void carregarRanking() {
        main.getJogo().getRanking().atualizarDoServidor(
                main.getApiClient(),
                () -> Gdx.app.postRunnable(this::reconstruirTabela),
                () -> Gdx.app.postRunnable(() -> {
                    statusLabel.setColor(Palette.ERRO);
                    statusLabel.setText("Não foi possível carregar o ranking.");
                }));
    }

    private void reconstruirTabela() {
        listaTabela.clearChildren();
        List<Jogador> jogadores = main.getJogo().getRanking().getRankingJogadores();
        Jogador eu = main.getJogo().getJogadorLogado();
        Long meuId = eu != null ? eu.getId() : null;

        if (jogadores == null || jogadores.isEmpty()) {
            statusLabel.setText("Nenhum jogador no ranking ainda.");
            return;
        }
        statusLabel.setText("");

        int posicao = 1;
        for (Jogador j : jogadores) {
            boolean souEu = meuId != null && meuId.equals(j.getId());
            Color cor = souEu ? Palette.OURO : Palette.TEXTO_CLARO;

            Label lPos = new Label(posicao + "º", main.getSkin());
            Label lNome = new Label(j.getUsername(), main.getSkin());
            Label lTempo = new Label(j.getTempoEstudado() + " min", main.getSkin());
            lPos.setColor(cor);
            lNome.setColor(cor);
            lTempo.setColor(cor);

            listaTabela.add(lPos).width(50f).left().pad(4f);
            listaTabela.add(lNome).width(240f).left().pad(4f);
            listaTabela.add(lTempo).width(100f).right().pad(4f);
            listaTabela.row();
            posicao++;
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Palette.ceu(main.getJogo().getCicloDiaNoite().fatorNoite()));
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override public void pause() { }
    @Override public void resume() { }

    @Override
    public void hide() {
        if (Gdx.input.getInputProcessor() == stage) {
            Gdx.input.setInputProcessor(null);
        }
    }

    @Override
    public void dispose() {
        if (stage != null) {
            stage.dispose();
        }
    }
}
