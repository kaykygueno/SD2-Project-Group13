package com.Griffith;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class FirstScreen implements Screen {

    private ShapeRenderer shapeRenderer;

   Player fireboy;
    Player watergirl;

    float groundY = 50;

    // Hazard (water pool)
    float hazardX = 300;
    float hazardWidth = 100;

    // Goal (door)
    float doorX = 550;

    @Override
    public void show() {
        shapeRenderer = new ShapeRenderer();

        fireboy = new Player(100, 100, Input.Keys.A, Input.Keys.D, Input.Keys.W);
        watergirl = new Player(200, 100, Input.Keys.LEFT, Input.Keys.RIGHT, Input.Keys.UP);
    }

    @Override
    public void render(float delta) {

        // CLEAR SCREEN
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // UPDATE PLAYERS
        fireboy.update(delta, groundY);
        watergirl.update(delta, groundY);

        // HAZARD CHECK
        if (fireboy.x > hazardX && fireboy.x < hazardX + hazardWidth && fireboy.y <= groundY) {
            System.out.println("Fireboy died in water!");
        }

        // GOAL CHECK
        if (fireboy.x > doorX && watergirl.x > doorX) {
            System.out.println("Level Complete!");
        }

        // DRAW
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Ground
        shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 1);
        shapeRenderer.rect(0, 0, 800, groundY);

        // Fireboy (red)
        shapeRenderer.setColor(1, 0, 0, 1);
        shapeRenderer.rect(fireboy.x, fireboy.y, 30, 30);

        // Watergirl (blue)
        shapeRenderer.setColor(0, 0, 1, 1);
        shapeRenderer.rect(watergirl.x, watergirl.y, 30, 30);

        // Hazard (cyan water)
        shapeRenderer.setColor(0, 1, 1, 1);
        shapeRenderer.rect(hazardX, groundY, hazardWidth, 30);

        // Door (green)
        shapeRenderer.setColor(0, 1, 0, 1);
        shapeRenderer.rect(doorX, groundY, 30, 60);

        shapeRenderer.end();
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}
