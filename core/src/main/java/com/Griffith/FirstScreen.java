package com.Griffith;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class FirstScreen implements Screen {

    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;

    Player fireboy;
    Player watergirl;

    float groundY = 50;

    // Water hazard (kills Fireboy)
    float waterHazardX = 300;
    float waterHazardWidth = 100;

    // Lava hazard (kills Watergirl)
    float lavaHazardX = 450;
    float lavaHazardWidth = 100;

    // Goal (door)
    float doorX = 650;

    boolean gameOver = false;
    boolean levelComplete = false;
    String gameMessage = "";

    @Override
    public void show() {
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();

        fireboy = new Player(100, 100, Input.Keys.A, Input.Keys.D, Input.Keys.W);
        watergirl = new Player(200, 100, Input.Keys.LEFT, Input.Keys.RIGHT, Input.Keys.UP);
    }

    @Override
    public void render(float delta) {
        // Restart
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            resetGame();
        }

        // Clear screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update only if game is still active
        if (!gameOver && !levelComplete) {
            fireboy.update(delta, groundY);
            watergirl.update(delta, groundY);

            // Fireboy dies in water
            if (fireboy.x + 30 > waterHazardX && fireboy.x < waterHazardX + waterHazardWidth && fireboy.y <= groundY + 30) {
                gameOver = true;
                gameMessage = "Game Over! Fireboy fell into water. Press R to restart.";
            }

            // Watergirl dies in lava
            if (watergirl.x + 30 > lavaHazardX && watergirl.x < lavaHazardX + lavaHazardWidth && watergirl.y <= groundY + 30) {
                gameOver = true;
                gameMessage = "Game Over! Watergirl fell into lava. Press R to restart.";
            }

            // Level complete
            if (fireboy.x > doorX && watergirl.x > doorX) {
                levelComplete = true;
                gameMessage = "Level Complete! Press R to play again.";
            }
        }

        // Draw shapes
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Ground
        shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 1);
        shapeRenderer.rect(0, 0, 800, groundY);

        // Water hazard
        shapeRenderer.setColor(0, 1, 1, 1);
        shapeRenderer.rect(waterHazardX, groundY, waterHazardWidth, 30);

        // Lava hazard
        shapeRenderer.setColor(1, 0.4f, 0, 1);
        shapeRenderer.rect(lavaHazardX, groundY, lavaHazardWidth, 30);

        // Door
        shapeRenderer.setColor(0, 1, 0, 1);
        shapeRenderer.rect(doorX, groundY, 30, 60);

        // Fireboy
        shapeRenderer.setColor(1, 0, 0, 1);
        shapeRenderer.rect(fireboy.x, fireboy.y, 30, 30);

        // Watergirl
        shapeRenderer.setColor(0, 0, 1, 1);
        shapeRenderer.rect(watergirl.x, watergirl.y, 30, 30);

        shapeRenderer.end();

        // Draw UI text
        batch.begin();

        font.setColor(Color.WHITE);
        font.draw(batch, "Level 1", 20, 460);
        font.draw(batch, "Fireboy: A/D to move, W to jump", 20, 430);
        font.draw(batch, "Watergirl: Arrow keys to move, UP to jump", 20, 400);
        font.draw(batch, "Press R to restart", 20, 370);

        if (!gameMessage.isEmpty()) {
            font.setColor(Color.YELLOW);
            font.draw(batch, gameMessage, 20, 330);
        }

        batch.end();
    }

    private void resetGame() {
        fireboy.x = 100;
        fireboy.y = 100;
        fireboy.velocityY = 0;

        watergirl.x = 200;
        watergirl.y = 100;
        watergirl.velocityY = 0;

        gameOver = false;
        levelComplete = false;
        gameMessage = "";
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
        batch.dispose();
        font.dispose();
    }
}