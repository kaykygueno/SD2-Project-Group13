package com.Griffith;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class MenuScreen implements Screen {
    private Main game;
    private SpriteBatch batch;
    private BitmapFont font;
    private boolean win;

    public MenuScreen(Main game, boolean win) {
        this.game = game;
        this.win = win;
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(2f);
    }

    public MenuScreen(Main game) { this(game, false); }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        if(win) font.draw(batch, "TEBRIKLER KAZANDINIZ!", 200, 300);
        else font.draw(batch, "ATES VE SU - BASLAMAK ICIN ENTER", 150, 300);
        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) game.setScreen(new FirstScreen(game));
    }

    @Override public void show() {}
    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() { batch.dispose(); font.dispose(); }
}