package com.Griffith.Sprites;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

// This class represents the player character in the game, handling movement, jumping, and collision with the environment.
public class Player {

    public float x, y;
    public float velocityY;
    public boolean isDead = false;

    private static final float SPEED = 150f;
    private static final float JUMP_POWER = 220f;
    private static final float GRAVITY = -500f;
    public static final float WIDTH = 16f;
    public static final float HEIGHT = 16f;

    private int leftKey, rightKey, jumpKey;
    private boolean onGround = false;

    private Texture texture;
    public Rectangle bounds;

    private float spawnX, spawnY;

    public Player(float x, float y, String texturePath, int leftKey, int rightKey, int jumpKey) {
        this.x = x;
        this.y = y;
        this.spawnX = x;
        this.spawnY = y;
        this.leftKey = leftKey;
        this.rightKey = rightKey;
        this.jumpKey = jumpKey;
        this.texture = new Texture(texturePath);
        this.bounds = new Rectangle(x, y, WIDTH, HEIGHT);
    }

    public void update(float delta, Array<Rectangle> ground) {
        if (isDead)
            return;

        float moveX = 0f;
        if (Gdx.input.isKeyPressed(leftKey)) {
            moveX -= SPEED * delta;
        }
        if (Gdx.input.isKeyPressed(rightKey)) {
            moveX += SPEED * delta;
        }

        x += moveX;
        bounds.setPosition(x, y);

        for (Rectangle tile : ground) {
            if (bounds.overlaps(tile)) {
                if (moveX > 0) {
                    x = tile.x - WIDTH;
                } else if (moveX < 0) {
                    x = tile.x + tile.width;
                }
                bounds.setPosition(x, y);
            }
        }

        if (Gdx.input.isKeyJustPressed(jumpKey) && onGround) {
            velocityY = JUMP_POWER;
            onGround = false;
        }

        velocityY += GRAVITY * delta;
        y += velocityY * delta;
        bounds.setPosition(x, y);

        onGround = false;

        for (Rectangle tile : ground) {
            if (bounds.overlaps(tile)) {
                if (velocityY < 0) {
                    y = tile.y + tile.height;
                    velocityY = 0;
                    onGround = true;
                } else if (velocityY > 0) {
                    y = tile.y - HEIGHT;
                    velocityY = 0;
                }
                bounds.setPosition(x, y);
            }
        }

        if (y <= 0) {
            y = 0;
            velocityY = 0;
            onGround = true;
            bounds.setPosition(x, y);
        }
    }

    public void moveBy(float dx, float dy) {
        x += dx;
        y += dy;
        bounds.setPosition(x, y);
    }

    public void draw(SpriteBatch batch) {
        if (!isDead) {
            batch.draw(texture, x, y, WIDTH, HEIGHT);
        }
    }

    public void die() {
        isDead = true;
        velocityY = 0;
    }

    public void respawn() {
        x = spawnX;
        y = spawnY;
        velocityY = 0;
        isDead = false;
        onGround = false;
        bounds.setPosition(x, y);
    }

    public void setOnGround(boolean val) {
        this.onGround = val;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void dispose() {
        texture.dispose();
    }
}