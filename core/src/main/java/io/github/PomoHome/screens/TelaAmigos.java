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
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.PomoHome.Main;
import io.github.PomoHome.model.Jogador;
import io.github.PomoHome.model.SolicitacaoAmizade;
import io.github.PomoHome.network.ApiClient;

import java.util.List;

/**
 * Friends screen (scene2d): search a player by username and send a request,
 * answer pending requests (accept/reject), and browse the friends list with a
 * "Visitar" button per friend. All actions hit the backend's
 * {@code /api/solicitacoes} + {@code /api/jogadores} endpoints.
 */
public class TelaAmigos implements Screen {

    private final Main main;
    private final ApiClient api;
    private Stage stage;

    private TextField campoBusca;
    private Label statusBusca;
    private Table solicitacoesTabela;
    private Table amigosTabela;

    public TelaAmigos(Main main) {
        this.main = main;
        this.api = main.getApiClient();
    }

    private Jogador eu() {
        return main.getJogo().getJogadorLogado();
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

        Label titulo = new Label("Amigos", main.getSkin());

        // --- Search / add ---
        campoBusca = new TextField("", main.getSkin());
        campoBusca.setMessageText("usuário");
        TextButton botaoBuscar = new TextButton("Adicionar", main.getSkin());
        botaoBuscar.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { buscarEEnviar(); }
        });
        statusBusca = new Label("", main.getSkin());
        statusBusca.setColor(Color.LIGHT_GRAY);

        Table linhaBusca = new Table();
        linhaBusca.add(campoBusca).width(220f).height(40f).padRight(10f);
        linhaBusca.add(botaoBuscar).width(150f).height(40f);

        // --- Sections ---
        solicitacoesTabela = new Table();
        amigosTabela = new Table();
        ScrollPane scrollSolic = new ScrollPane(solicitacoesTabela, main.getSkin());
        ScrollPane scrollAmigos = new ScrollPane(amigosTabela, main.getSkin());
        scrollSolic.setFadeScrollBars(false);
        scrollAmigos.setFadeScrollBars(false);

        TextButton voltar = new TextButton("Voltar", main.getSkin());
        voltar.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { main.voltarAoJogo(); }
        });

        root.add(titulo).padBottom(16f).row();
        root.add(linhaBusca).padBottom(6f).row();
        root.add(statusBusca).padBottom(14f).row();
        root.add(new Label("Solicitações recebidas", main.getSkin())).left().padBottom(4f).row();
        root.add(scrollSolic).width(460f).height(140f).padBottom(14f).row();
        root.add(new Label("Meus amigos", main.getSkin())).left().padBottom(4f).row();
        root.add(scrollAmigos).width(460f).height(180f).padBottom(16f).row();
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

        recarregar();
    }

    /** Refresh the logged player's amigosIds, the inbox, and the friends list. */
    private void recarregar() {
        Jogador eu = eu();
        if (eu == null || eu.getId() == null) {
            return;
        }
        // Refresh amigosIds in place (don't swap the Jogador instance — the game
        // screen holds references to it).
        api.fetchJogadorPorId(eu.getId(), new ApiClient.Callback<Jogador>() {
            @Override public void onSuccess(Jogador atualizado) {
                Gdx.app.postRunnable(() -> {
                    eu.setAmigosIds(atualizado.getAmigosIds());
                    eu.setSaldo(atualizado.getSaldo());
                    reconstruirAmigos();
                });
            }
            @Override public void onError(Throwable t) { }
        });
        recarregarSolicitacoes();
    }

    private void recarregarSolicitacoes() {
        Jogador eu = eu();
        api.listarSolicitacoesRecebidas(eu.getId(), new ApiClient.Callback<List<SolicitacaoAmizade>>() {
            @Override public void onSuccess(List<SolicitacaoAmizade> result) {
                Gdx.app.postRunnable(() -> reconstruirSolicitacoes(result));
            }
            @Override public void onError(Throwable t) { }
        });
    }

    // ---------------------------------------------------------------
    // Search + send request
    // ---------------------------------------------------------------

    private void buscarEEnviar() {
        final String alvo = campoBusca.getText().trim();
        Jogador eu = eu();
        if (alvo.isEmpty() || eu == null || eu.getId() == null) {
            return;
        }
        if (alvo.equalsIgnoreCase(eu.getUsername())) {
            mostrarStatus("Você não pode se adicionar.", Color.SCARLET);
            return;
        }
        mostrarStatus("Buscando...", Color.LIGHT_GRAY);
        api.fetchJogadorPorUsername(alvo, new ApiClient.Callback<Jogador>() {
            @Override public void onSuccess(Jogador encontrado) {
                api.enviarSolicitacao(eu.getId(), encontrado.getId(),
                        new ApiClient.Callback<SolicitacaoAmizade>() {
                            @Override public void onSuccess(SolicitacaoAmizade s) {
                                Gdx.app.postRunnable(() -> {
                                    mostrarStatus("Solicitação enviada para " + encontrado.getUsername(), Color.LIME);
                                    campoBusca.setText("");
                                });
                            }
                            @Override public void onError(Throwable t) {
                                Gdx.app.postRunnable(() -> mostrarStatus(t.getMessage(), Color.SCARLET));
                            }
                        });
            }
            @Override public void onError(Throwable t) {
                Gdx.app.postRunnable(() -> mostrarStatus("Usuário não encontrado.", Color.SCARLET));
            }
        });
    }

    // ---------------------------------------------------------------
    // Inbox rows
    // ---------------------------------------------------------------

    private void reconstruirSolicitacoes(List<SolicitacaoAmizade> pedidos) {
        solicitacoesTabela.clearChildren();
        if (pedidos == null || pedidos.isEmpty()) {
            solicitacoesTabela.add(new Label("Nenhuma solicitação.", main.getSkin())).left().pad(4f);
            return;
        }
        for (SolicitacaoAmizade s : pedidos) {
            final Label nome = new Label("#" + s.getRemetenteId(), main.getSkin());
            // Resolve the requester's username asynchronously.
            api.fetchJogadorPorId(s.getRemetenteId(), new ApiClient.Callback<Jogador>() {
                @Override public void onSuccess(Jogador r) {
                    Gdx.app.postRunnable(() -> nome.setText(r.getUsername()));
                }
                @Override public void onError(Throwable t) { }
            });

            TextButton aceitar = new TextButton("Aceitar", main.getSkin());
            TextButton recusar = new TextButton("Recusar", main.getSkin());
            aceitar.addListener(new ChangeListener() {
                @Override public void changed(ChangeEvent event, Actor actor) { responder(s, true); }
            });
            recusar.addListener(new ChangeListener() {
                @Override public void changed(ChangeEvent event, Actor actor) { responder(s, false); }
            });

            solicitacoesTabela.add(nome).width(220f).left().pad(4f);
            solicitacoesTabela.add(aceitar).width(110f).pad(4f);
            solicitacoesTabela.add(recusar).width(110f).pad(4f);
            solicitacoesTabela.row();
        }
    }

    private void responder(SolicitacaoAmizade s, boolean aceitar) {
        ApiClient.Callback<SolicitacaoAmizade> cb = new ApiClient.Callback<SolicitacaoAmizade>() {
            @Override public void onSuccess(SolicitacaoAmizade result) {
                Gdx.app.postRunnable(TelaAmigos.this::recarregar);
            }
            @Override public void onError(Throwable t) {
                Gdx.app.postRunnable(() -> mostrarStatus(t.getMessage(), Color.SCARLET));
            }
        };
        if (aceitar) {
            api.aceitarSolicitacao(s.getId(), cb);
        } else {
            api.recusarSolicitacao(s.getId(), cb);
        }
    }

    // ---------------------------------------------------------------
    // Friends rows
    // ---------------------------------------------------------------

    private void reconstruirAmigos() {
        amigosTabela.clearChildren();
        Jogador eu = eu();
        List<Long> ids = eu != null ? eu.getAmigosIds() : null;
        if (ids == null || ids.isEmpty()) {
            amigosTabela.add(new Label("Você ainda não tem amigos.", main.getSkin())).left().pad(4f);
            return;
        }
        for (Long amigoId : ids) {
            final Label nome = new Label("#" + amigoId, main.getSkin());
            TextButton visitar = new TextButton("Visitar", main.getSkin());
            visitar.addListener(new ChangeListener() {
                @Override public void changed(ChangeEvent event, Actor actor) {
                    main.setScreen(new TelaVisita(main, amigoId, nome.getText().toString()));
                }
            });
            api.fetchJogadorPorId(amigoId, new ApiClient.Callback<Jogador>() {
                @Override public void onSuccess(Jogador r) {
                    Gdx.app.postRunnable(() -> nome.setText(r.getUsername()));
                }
                @Override public void onError(Throwable t) { }
            });

            amigosTabela.add(nome).width(300f).left().pad(4f);
            amigosTabela.add(visitar).width(120f).pad(4f);
            amigosTabela.row();
        }
    }

    private void mostrarStatus(String texto, Color cor) {
        statusBusca.setColor(cor);
        statusBusca.setText(texto == null ? "" : texto);
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
