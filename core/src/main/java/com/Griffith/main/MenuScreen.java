package com.Griffith.main;

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

    // This constructor initializes the menu screen and configures whether it should show the win message.
    public MenuScreen(Main game, boolean win) {
        this.game = game;
        this.win = win;
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(2f);
    }

    // This constructor creates the default non-win version of the menu screen.
    public MenuScreen(Main game) {
        this(game, false);
    }

    // This method draws the menu text and starts the gameplay screen when Enter is pressed.
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        if (win)
            font.draw(batch, "TEBRIKLER KAZANDINIZ!", 200, 300);
        else
            font.draw(batch, "ATES VE SU - BASLAMAK ICIN ENTER", 150, 300);
        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER))
            game.setScreen(new FirstScreen());
    }

    // This method is called when the menu screen becomes active.
    @Override
    public void show() {
    }

    // This method is called when the menu screen viewport changes size.
    @Override
    public void resize(int width, int height) {
    }

    // This method is called when the application is paused on the menu screen.
    @Override
    public void pause() {
    }

    // This method is called when the application resumes while the menu screen is active.
    @Override
    public void resume() {
    }

    // This method is called when the menu screen is hidden.
    @Override
    public void hide() {
    }

    // This method disposes the menu rendering resources.
    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}
