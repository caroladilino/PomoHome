package io.github.PomoHome.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.PomoHome.Main;
import io.github.PomoHome.model.Jogador;

/**
 * Landing screen shown right after a successful login. It confirms the
 * post-login handoff works (the authenticated {@link Jogador} is in the
 * session) and is the placeholder where the team will wire the real main
 * menu / navigation to TelaPomodoro, TelaLoja, TelaCasa, TelaRanking.
 */
public class TelaPrincipal implements Screen {

    private final Main main;
    private Stage stage;

    public TelaPrincipal(Main main) {
        this.main = main;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        Jogador j = main.getJogo().getJogadorLogado();
        String nome = j != null ? j.getUsername() : "?";
        int saldo = j != null ? j.getSaldo() : 0;

        Table root = new Table();
        root.setFillParent(true);
        root.pad(24f);
        stage.addActor(root);

        Label bemVindo = new Label("Bem-vindo, " + nome + "!", main.getSkin());
        bemVindo.setColor(Color.WHITE);

        Label info = new Label(saldo + " moedas  ·  menu principal em construção",
                main.getSkin());
        info.setColor(Color.LIGHT_GRAY);

        root.add(bemVindo).padBottom(12f).row();
        root.add(info).row();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.12f, 0.12f, 0.16f, 1f);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override public void pause()  { }
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
