package com.Griffith;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class Player {

    public float x, y;
    public float velocityY;
    public boolean isDead = false;

    private static final float SPEED = 150f;
    private static final float JUMP_POWER = 250f;
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

        float previousY = y;

        // Horizontal movement
        if (Gdx.input.isKeyPressed(leftKey))
            x -= SPEED * delta;
        else if (Gdx.input.isKeyPressed(rightKey))
            x += SPEED * delta;

        // Jump
        if (Gdx.input.isKeyJustPressed(jumpKey) && onGround) {
            velocityY = JUMP_POWER;
            onGround = false;
        }

        // Gravity
        velocityY += GRAVITY * delta;
        y += velocityY * delta;

        // Ground collision using previous and new positions to prevent tunneling.
        onGround = false;
        bounds.setPosition(x, y);
        float previousBottom = previousY;
        float currentBottom = y;
        float playerLeft = x;
        float playerRight = x + WIDTH;

        for (Rectangle tile : ground) {
            float tileTop = tile.y + tile.height;
            float tileLeft = tile.x;
            float tileRight = tile.x + tile.width;

            boolean hasHorizontalOverlap = playerRight > tileLeft && playerLeft < tileRight;
            boolean crossedTileTop = previousBottom >= tileTop && currentBottom <= tileTop;

            if (velocityY <= 0 && hasHorizontalOverlap && crossedTileTop) {
                y = tileTop;
                velocityY = 0;
                onGround = true;
                break;
            }
        }

        // Fallback: world floor
        if (y <= 0) {
            y = 0;
            velocityY = 0;
            onGround = true;
        }

        bounds.setPosition(x, y);
    }

    public void draw(SpriteBatch batch) {
        if (!isDead)
            batch.draw(texture, x, y, WIDTH, HEIGHT);
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