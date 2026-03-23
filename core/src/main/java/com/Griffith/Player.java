package com.Griffith;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class Player {

    public float x, y;
    public float velocityY;

    private float speed = 200;
    private float jumpPower = 300;
    private float gravity = -500;

    private int leftKey;
    private int rightKey;
    private int jumpKey;

    public Player(float x, float y, int leftKey, int rightKey, int jumpKey) {
        this.x = x;
        this.y = y;
        this.leftKey = leftKey;
        this.rightKey = rightKey;
        this.jumpKey = jumpKey;
    }

    public void update(float delta, float groundY) {

        // Movement
        if (Gdx.input.isKeyPressed(leftKey)) x -= speed * delta;
        if (Gdx.input.isKeyPressed(rightKey)) x += speed * delta;

        // Jump
        if (Gdx.input.isKeyJustPressed(jumpKey) && y <= groundY) {
            velocityY = jumpPower;
        }

        // Gravity
        velocityY += gravity * delta;
        y += velocityY * delta;

        // Ground collision
        if (y < groundY) {
            y = groundY;
            velocityY = 0;
        }
    }
}