package com.Griffith.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class MenuScreen implements Screen {

    private final Main game;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final GlyphLayout layout;

    private OrthographicCamera camera;
    private Viewport viewport;

    private final boolean win;

    public MenuScreen(Main game, boolean win) {
        this.game = game;
        this.win = win;

        batch = new SpriteBatch();
        font = new BitmapFont();
        layout = new GlyphLayout();

        font.getData().setScale(2f);
    }

    public MenuScreen(Main game) {
        this(game, false);
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(800, 600, camera); // resolução base
        viewport.apply();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(camera.combined);

        String title = win ? "CONGRATULATIONS!" : "FIRE AND WATER";
        String subtitle = win ? "PRESS ENTER TO PLAY AGAIN" : "PRESS ENTER TO START";

        // centralização com viewport
        layout.setText(font, title);
        float titleX = (viewport.getWorldWidth() - layout.width) / 2f;
        float titleY = viewport.getWorldHeight() / 2f + 40;

        layout.setText(font, subtitle);
        float subX = (viewport.getWorldWidth() - layout.width) / 2f;
        float subY = viewport.getWorldHeight() / 2f - 20;

        batch.begin();
        font.draw(batch, title, titleX, titleY);
        font.draw(batch, subtitle, subX, subY);
        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            game.setScreen(new FirstScreen(game));
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true); // 🔥 ISSO FAZ FUNCIONAR
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}