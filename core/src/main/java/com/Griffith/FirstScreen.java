package com.Griffith;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class FirstScreen implements Screen {

    private ShapeRenderer shapeRenderer;

    // Fireboy
    float fireX = 100, fireY = 100;
    float fireVelocityY = 0;

    // Watergirl
    float waterX = 200, waterY = 100;
    float waterVelocityY = 0;

    float speed = 200;
    float gravity = -500;
    float jumpPower = 300;

    float groundY = 50;

    // Hazard (water pool)
    float hazardX = 300;
    float hazardWidth = 100;

      // Goal (door)
    float doorX = 550;

    @Override
    public void show() {
        shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void render(float delta) {

        // Clear screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // INPUTS FOR MOVEMENT 

        // Fireboy (A, D)
        if (Gdx.input.isKeyPressed(Input.Keys.A)) fireX -= speed * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) fireX += speed * delta;

        // Watergirl (LEFT, RIGHT)
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) waterX -= speed * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) waterX += speed * delta;

        // JUMP

        // Fireboy jump (W)
        if (Gdx.input.isKeyJustPressed(Input.Keys.W) && fireY <= groundY) {
            fireVelocityY = jumpPower;
        }
        // Watergirl jump (UP)
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) && waterY <= groundY) {
            waterVelocityY = jumpPower;
        }

        // GRAVITY

        fireVelocityY += gravity * delta;
        fireY += fireVelocityY * delta;

        waterVelocityY += gravity * delta;
        waterY += waterVelocityY * delta;

        // GROUND COLLISION

        if (fireY < groundY) {
            fireY = groundY;
            fireVelocityY = 0;
        }

        if (waterY < groundY) {
            waterY = groundY;
            waterVelocityY = 0;
        }

        // HAZARD CHECK

        if (fireX > hazardX && fireX < hazardX + hazardWidth && fireY <= groundY) {
            System.out.println("Fireboy died in water!");
        }

        // GOAL CHECK

        if (fireX > doorX && waterX > doorX) {
            System.out.println("Level Complete!");
        }

        // DRAW

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Ground
        shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 1);
        shapeRenderer.rect(0, 0, 800, groundY);

        // Fireboy (red)
        shapeRenderer.setColor(1, 0, 0, 1);
        shapeRenderer.rect(fireX, fireY, 30, 30);

        // Watergirl (blue)
        shapeRenderer.setColor(0, 0, 1, 1);
        shapeRenderer.rect(waterX, waterY, 30, 30);

        // Hazard (cyan)
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
