package io.github.PomoHome.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.PomoHome.Main;
import io.github.PomoHome.model.Jogador;
import io.github.PomoHome.network.ApiClient;

/**
 * Startup screen. The user either logs in or creates a new account, both
 * against the Spring Boot backend (POST /api/jogadores/login and
 * POST /api/jogadores). Server errors (e.g. "Username já existe",
 * "Credenciais inválidas") are shown verbatim under the form.
 *
 * On success the returned {@link Jogador} is handed to {@link Main} via
 * {@code entrarNoJogo}, which stores it in the session and switches screen.
 */
public class TelaLogin implements Screen {

    private final Main main;
    private final ApiClient api;

    private Stage stage;
    private TextField campoUsuario;
    private TextField campoSenha;
    private Label mensagem;
    private TextButton botaoEntrar;
    private TextButton botaoCriar;

    /** Guards against firing a second request before the first answers. */
    private boolean ocupado = false;

    public TelaLogin(Main main) {
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
        stage.addActor(root);

        Label titulo = new Label("PomoHome", main.getSkin());
        titulo.setColor(Color.WHITE);

        campoUsuario = new TextField("", main.getSkin());
        campoUsuario.setMessageText("usuário");

        campoSenha = new TextField("", main.getSkin());
        campoSenha.setMessageText("senha");
        campoSenha.setPasswordMode(true);
        campoSenha.setPasswordCharacter('*');

        mensagem = new Label("", main.getSkin());
        mensagem.setColor(Color.SCARLET);
        mensagem.setWrap(true);

        botaoEntrar = new TextButton("Entrar", main.getSkin());
        botaoCriar = new TextButton("Criar conta", main.getSkin());

        botaoEntrar.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                submeter(false);
            }
        });
        botaoCriar.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                submeter(true);
            }
        });

        float w = 320f;
        root.add(titulo).padBottom(28f).row();
        root.add(campoUsuario).width(w).height(40f).padBottom(10f).row();
        root.add(campoSenha).width(w).height(40f).padBottom(16f).row();

        Table botoes = new Table();
        botoes.add(botaoEntrar).width(150f).padRight(10f);
        botoes.add(botaoCriar).width(150f);
        root.add(botoes).padBottom(14f).row();

        root.add(mensagem).width(w).row();

        // Enter anywhere on the form attempts a login.
        stage.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override
            public boolean keyDown(com.badlogic.gdx.scenes.scene2d.InputEvent event, int keycode) {
                if (keycode == Input.Keys.ENTER || keycode == Input.Keys.NUMPAD_ENTER) {
                    submeter(false);
                    return true;
                }
                return false;
            }
        });

        stage.setKeyboardFocus(campoUsuario);
    }

    /** @param criarConta true = create account, false = login. */
    private void submeter(boolean criarConta) {
        if (ocupado) {
            return;
        }
        final String usuario = campoUsuario.getText().trim();
        final String senha = campoSenha.getText();

        if (usuario.isEmpty() || senha.isEmpty()) {
            mostrarErro("Preencha usuário e senha.");
            return;
        }

        setOcupado(true);
        mensagem.setColor(Color.LIGHT_GRAY);
        mensagem.setText(criarConta ? "Criando conta..." : "Entrando...");

        ApiClient.Callback<Jogador> cb = new ApiClient.Callback<Jogador>() {
            @Override
            public void onSuccess(final Jogador jogador) {
                // Off the GL thread — hop back before touching the game.
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        main.entrarNoJogo(jogador);
                    }
                });
            }

            @Override
            public void onError(final Throwable t) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        setOcupado(false);
                        mostrarErro(t.getMessage());
                    }
                });
            }
        };

        if (criarConta) {
            api.cadastrar(usuario, senha, cb);
        } else {
            api.login(usuario, senha, cb);
        }
    }

    private void mostrarErro(String texto) {
        mensagem.setColor(Color.SCARLET);
        mensagem.setText(texto == null ? "Erro inesperado." : texto);
    }

    private void setOcupado(boolean valor) {
        ocupado = valor;
        botaoEntrar.setDisabled(valor);
        botaoCriar.setDisabled(valor);
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
        // Stop feeding input to a screen we're leaving.
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
