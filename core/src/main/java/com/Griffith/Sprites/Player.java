package com.Griffith.Sprites;

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
    private static final float JUMP_POWER = 220f;
    private static final float GRAVITY = -500f;
    public static final float WIDTH = 16f;
    public static final float HEIGHT = 16f;

    private int leftKey, rightKey, jumpKey;
    private boolean onGround = false;

    private Texture texture;
    public Rectangle bounds;

    private float spawnX, spawnY;

    // This constructor stores the spawn setup, input bindings, and sprite used by the player.
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

    // This method handles player input, gravity, and collisions with the current ground colliders.
    public void update(float delta, Array<Rectangle> ground) {
        if (isDead) return;

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

    // This method shifts the player by the given offset, which is used when a lift carries the player.
    public void moveBy(float dx, float dy) {
        x += dx;
        y += dy;
        bounds.setPosition(x, y);
    }

    // This method draws the player sprite when the player is alive.
    public void draw(SpriteBatch batch) {
        if (!isDead) {
            batch.draw(texture, x, y, WIDTH, HEIGHT);
        }
    }

    // This method marks the player as dead and stops vertical movement.
    public void die() {
        isDead = true;
        velocityY = 0;
    }

    // This method returns the player to its original spawn position and clears death state.
    public void respawn() {
        x = spawnX;
        y = spawnY;
        velocityY = 0;
        isDead = false;
        onGround = false;
        bounds.setPosition(x, y);
    }

    // This method manually sets whether the player is considered grounded.
    public void setOnGround(boolean val) {
        this.onGround = val;
    }

    // This method returns the player's collision bounds.
    public Rectangle getBounds() {
        return bounds;
    }

    // This method releases the player texture when the screen is disposed.
    public void dispose() {
        texture.dispose();
    }
}
