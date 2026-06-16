package io.github.PomoHome.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import io.github.PomoHome.Main;
import io.github.PomoHome.model.Casa;
import io.github.PomoHome.network.ApiClient;
import io.github.PomoHome.ui.Palette;
import io.github.PomoHome.ui.actors.CasaActor;
import space.earlygrey.shapedrawer.ShapeDrawer;

/**
 * Read-only visit to a friend's house, fully scene2d. Fetches the friend's Casa
 * ({@code GET /api/casas/jogador/{id}}), rebuilds the grid from the persisted
 * slots and renders it with a non-editable {@link CasaActor} (drawn through the
 * Stage's batch via {@link ShapeDrawer}). Shows the house name + like count with
 * a like toggle ({@code POST /casas/{id}/like}, one per visitor) and a Voltar
 * button back to the friends list. The fixed 1280×720 viewport keeps the house
 * a stable size on resize.
 */
public class TelaVisita implements Screen {

    private static final float MUNDO_W = 1280f;
    private static final float MUNDO_H = 720f;

    private final Main main;
    private final ApiClient api;
    private final long amigoId;
    private final String nomeAmigo;

    private Stage stage;
    private ExtendViewport viewport;
    private ShapeDrawer drawer;
    private CasaActor casaActor;

    private Casa casa;
    private Long meuId;            // logged-in player (the one toggling the like)
    private boolean jaCurtiu = false;

    private Label lblTitulo, lblInfo;
    private TextButton btnLike, btnVoltar;

    public TelaVisita(Main main, long amigoId, String nomeAmigo) {
        this.main = main;
        this.api = main.getApiClient();
        this.amigoId = amigoId;
        this.nomeAmigo = nomeAmigo;
    }

    @Override
    public void show() {
        viewport = new ExtendViewport(MUNDO_W, MUNDO_H);
        // PolygonSpriteBatch so ShapeDrawer's filled diamonds render efficiently.
        stage = new Stage(viewport, new PolygonSpriteBatch());
        drawer = new ShapeDrawer(stage.getBatch(), main.getSkin().getRegion("white"));

        meuId = main.getJogo().getJogadorLogado() != null
                ? main.getJogo().getJogadorLogado().getId() : null;

        lblTitulo = new Label("Casa de " + (nomeAmigo != null ? nomeAmigo : "amigo"), main.getSkin());
        lblInfo = new Label("Carregando...", main.getSkin());
        btnLike = new TextButton("CURTIR", main.getSkin(), "rosa");
        btnVoltar = new TextButton("VOLTAR", main.getSkin(), "rosa");
        aoClicar(btnVoltar, () -> main.setScreen(new TelaAmigos(main)));
        aoClicar(btnLike, () -> {
            if (casa != null && casa.getId() != null && meuId != null) {
                darLike();
            }
        });

        stage.addActor(lblTitulo);
        stage.addActor(lblInfo);
        stage.addActor(btnLike);
        stage.addActor(btnVoltar);

        Gdx.input.setInputProcessor(stage);

        api.fetchCasaDoJogador(amigoId, new ApiClient.Callback<Casa>() {
            @Override public void onSuccess(Casa result) {
                Gdx.app.postRunnable(() -> {
                    casa = result;
                    if (casa != null) {
                        casa.fromSlots();
                        jaCurtiu = meuId != null && casa.getCurtidoPor() != null
                                && casa.getCurtidoPor().contains(meuId);
                        casaActor = new CasaActor(casa, false, drawer,
                                main.getSkin().getFont("default-font"));
                        casaActor.reconstruirDoModelo();
                        stage.addActor(casaActor);
                    } else {
                        lblInfo.setText("Casa não encontrada.");
                    }
                });
            }
            @Override public void onError(Throwable t) {
                Gdx.app.postRunnable(() -> lblInfo.setText("Falha ao carregar a casa."));
            }
        });
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Palette.ceu(main.getJogo().getCicloDiaNoite().fatorNoite()));
        float w = viewport.getWorldWidth();
        float h = viewport.getWorldHeight();

        lblTitulo.setPosition(40f, h - 50f);
        lblInfo.setPosition(40f, h - 80f);
        btnVoltar.setBounds(40f, 122f, 150f, 46f);
        btnLike.setBounds(40f, 66f, 150f, 46f);
        btnLike.setText(jaCurtiu ? "DESCURTIR" : "CURTIR");
        if (casa != null) {
            lblInfo.setText(casa.getNome() + "  -  " + casa.getNumLikes() + " likes");
        }
        if (casaActor != null) {
            // House occupies the area right of the buttons.
            casaActor.setBounds(w * 0.2f, 90f, w * 0.8f - 40f, h - 90f - 130f);
        }

        stage.act(delta);
        stage.draw();
    }

    private void darLike() {
        api.darLike(casa.getId(), meuId, new ApiClient.Callback<Casa>() {
            @Override public void onSuccess(Casa result) {
                Gdx.app.postRunnable(() -> {
                    if (result != null) {
                        casa.setNumLikes(result.getNumLikes());
                        casa.setCurtidoPor(result.getCurtidoPor());
                        jaCurtiu = meuId != null && result.getCurtidoPor() != null
                                && result.getCurtidoPor().contains(meuId);
                    }
                });
            }
            @Override public void onError(Throwable t) {
                Gdx.app.postRunnable(() -> lblInfo.setText("Falha ao curtir."));
            }
        });
    }

    private void aoClicar(TextButton botao, Runnable acao) {
        botao.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                acao.run();
            }
        });
    }

    @Override
    public void resize(int width, int height) {
        if (viewport != null) {
            viewport.update(width, height, true);
        }
    }

    @Override public void pause() { }
    @Override public void resume() { }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        if (stage != null) {
            stage.dispose();
            stage = null;
        }
        // The Skin (font + white region) is owned by Main — not disposed here.
    }
}
