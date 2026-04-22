package io.github.PomoHome;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private BitmapFont bitmap;
    private Jogador jogador;

    private Vector2 cliqueMouse;
    //    private ArrayList<Texture> textures;
    private Texture texture;
    private final float LARGURA_SLOT = 100f;
    private final float ALTURA_SLOT = 100f;
    private final float ESPACAMENTO = 25f;
    private final float POSICAO_INICIAL_X = 50f;
    private final float POSICAO_CASA_Y = 300f;
    private final float POSICAO_INVENTARIO_Y = 500f;

    private Sound error;

    @Override
    public void create() {
        batch = new SpriteBatch();
        bitmap = new BitmapFont();

        cliqueMouse = new Vector2();
        texture = new Texture("teste.png");
        cliqueMouse = new Vector2();

        jogador = new Jogador("Eu");

        error = Gdx.audio.newSound(Gdx.files.internal("error.mp3"));
    }

    @Override
    public void render() {
        input();
        logic();
        draw();
    }

    public void input(){
        if (Gdx.input.justTouched()) {
            cliqueMouse.set(Gdx.input.getX(), Gdx.input.getY());
            ArrayList<Movel> iventario = jogador.getInventario();
            for (int i = 0; i < iventario.size(); i++) {
                float posX = POSICAO_INICIAL_X + ((i+1) * (LARGURA_SLOT + ESPACAMENTO));
                Rectangle hitbox = new Rectangle(posX, Gdx.graphics.getHeight() - POSICAO_INVENTARIO_Y - ALTURA_SLOT, LARGURA_SLOT, ALTURA_SLOT);

                if (hitbox.contains(cliqueMouse)) {
                    Movel movelClicado = iventario.get(i);
                    boolean sucesso = jogador.moverMovelParaCasa(movelClicado);
                    if (!sucesso) {
                        error.play();
                    }
                    break;
                }
            }
        }
    }

    public void logic(){

    }

    public void draw(){
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        batch.begin();

        Casa casa = jogador.getCasa();
        bitmap.draw(batch, casa.getNome(), POSICAO_INICIAL_X, POSICAO_CASA_Y + 50f);
        ArrayList<Movel> moveisCasa = casa.getMoveis();
        drawMoveis(moveisCasa, POSICAO_INICIAL_X, POSICAO_CASA_Y);

        bitmap.draw(batch, "Inventário:", POSICAO_INICIAL_X, POSICAO_INVENTARIO_Y + 50f);
        ArrayList<Movel> inventario = jogador.getInventario();
        drawMoveis(inventario, POSICAO_INICIAL_X, POSICAO_INVENTARIO_Y);

        batch.end();
    }

    public void drawMoveis(ArrayList<Movel> moveis, float inicio_x, float inicio_y) {
        for (int i = 0; i < moveis.size(); i++) {
            float posX = inicio_x + ((i+1) * (LARGURA_SLOT + ESPACAMENTO));
            batch.draw(texture, posX, inicio_y, LARGURA_SLOT, ALTURA_SLOT);
            bitmap.draw(batch, moveis.get(i).getNome(), posX, inicio_y);
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        bitmap.dispose();
    }
}
