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
import io.github.PomoHome.model.DiaEstudo;
import io.github.PomoHome.model.HistoricoSemanal;
import io.github.PomoHome.model.Jogador;
import io.github.PomoHome.network.ApiClient;
import io.github.PomoHome.ui.Palette;

import java.time.LocalDate;
import java.util.List;

/**
 * Private weekly-history screen (RF03, scene2d): the logged-in player's study
 * time for the current week, shown as a per-day bar chart (Mon→Sun) with a
 * weekly total, served by {@code GET /api/sessoes/jogador/{id}/semana}.
 *
 * <p>Private by design — it only ever requests the logged-in player's own id;
 * there is no way to view another player's history. "Voltar" returns to the
 * cached game screen.
 */
public class TelaHistorico implements Screen {

    private static final String[] DIAS = {"Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom"};

    private final Main main;
    private final ApiClient api;
    private Stage stage;
    private Table listaTabela;
    private Label statusLabel;
    private Label totalLabel;

    public TelaHistorico(Main main) {
        this.main = main;
        this.api = main.getApiClient();
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        Table root = new Table();
        root.setFillParent(true);
        root.pad(24f);
        root.top();
        stage.addActor(root);

        Label titulo = new Label("Histórico da Semana", main.getSkin());

        statusLabel = new Label("Carregando...", main.getSkin());
        statusLabel.setColor(Palette.NEUTRO);

        totalLabel = new Label("", main.getSkin());
        totalLabel.setColor(Palette.OURO);

        listaTabela = new Table();
        ScrollPane scroll = new ScrollPane(listaTabela, main.getSkin());
        scroll.setFadeScrollBars(false);

        TextButton voltar = new TextButton("Voltar", main.getSkin());
        voltar.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { main.voltarAoJogo(); }
        });

        root.add(titulo).padBottom(16f).row();
        root.add(statusLabel).padBottom(10f).row();
        root.add(scroll).width(460f).height(320f).padBottom(12f).row();
        root.add(totalLabel).padBottom(16f).row();
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

        carregar();
    }

    private void carregar() {
        Jogador eu = main.getJogo().getJogadorLogado();
        if (eu == null || eu.getId() == null) {
            statusLabel.setColor(Palette.ERRO);
            statusLabel.setText("Nenhum jogador logado.");
            return;
        }
        api.fetchHistoricoSemanal(eu.getId(), new ApiClient.Callback<HistoricoSemanal>() {
            @Override public void onSuccess(HistoricoSemanal h) {
                Gdx.app.postRunnable(() -> reconstruir(h));
            }
            @Override public void onError(Throwable t) {
                Gdx.app.postRunnable(() -> {
                    statusLabel.setColor(Palette.ERRO);
                    statusLabel.setText("Não foi possível carregar o histórico.");
                });
            }
        });
    }

    private void reconstruir(HistoricoSemanal h) {
        listaTabela.clearChildren();
        List<DiaEstudo> dias = h != null ? h.getDias() : null;
        if (dias == null || dias.isEmpty()) {
            statusLabel.setText("Sem dados para esta semana.");
            return;
        }
        statusLabel.setText("");

        int maxMinutos = 0;
        for (DiaEstudo d : dias) {
            maxMinutos = Math.max(maxMinutos, d.getMinutos());
        }
        int hojeIdx = LocalDate.now().getDayOfWeek().getValue() - 1; // Mon=0 .. Sun=6

        for (int i = 0; i < dias.size(); i++) {
            DiaEstudo d = dias.get(i);
            boolean hoje = i == hojeIdx;
            Color cor = hoje ? Palette.OURO : Palette.TEXTO_CLARO;

            String rotulo = i < DIAS.length ? DIAS[i] : "?";
            Label lDia = new Label(rotulo, main.getSkin());
            lDia.setColor(cor);

            // A bar whose width is proportional to the day's minutes (vs the
            // week's busiest day), so the relative effort reads at a glance.
            Table barra = new Table();
            barra.setBackground(main.getSkin().newDrawable("white",
                    hoje ? Palette.OURO : Palette.TERRACOTA));
            float larguraMax = 240f;
            float largura = maxMinutos > 0 ? larguraMax * d.getMinutos() / maxMinutos : 0f;

            Label lMin = new Label(d.getMinutos() + " min", main.getSkin());
            lMin.setColor(cor);

            listaTabela.add(lDia).width(48f).left().pad(4f);
            listaTabela.add(barra).width(Math.max(2f, largura)).height(14f).left().pad(4f);
            listaTabela.add(lMin).width(80f).right().pad(4f);
            listaTabela.row();
        }

        totalLabel.setText("Total da semana: " + h.getTotalMinutos() + " min");
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
