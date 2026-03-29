package com.Griffith;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Player {
    private Vector2 position;
    private Vector2 velocity;
    private Texture spriteSheet;
    private TextureRegion currentFrame;
    private boolean isFireboy, isGrounded = false, isDead = false;
    private TiledMap map;

    private final float SPEED = 140f;
    private final float GRAVITY = 700f;    
    private final float JUMP_FORCE = 300f; 

    private final float hitboxWidth = 24f;
    private final float hitboxHeight = 10f;

    public Player(float startX, float startY, String texturePath, boolean isFireboy, TiledMap map) {
        this.position = new Vector2(startX, startY);
        this.velocity = new Vector2(0, 0);
        this.isFireboy = isFireboy;
        this.map = map;
        this.spriteSheet = new Texture(Gdx.files.internal(texturePath));
        this.currentFrame = new TextureRegion(spriteSheet, 0, 0, 192, 192);
    }

    public void update(float deltaTime) {
        if (isDead) return;


        velocity.x = 0;
        if (isFireboy) { 
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) velocity.x = -SPEED;
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) velocity.x = SPEED;
            if (Gdx.input.isKeyJustPressed(Input.Keys.UP) && isGrounded) velocity.y = JUMP_FORCE;
        } else { 
            if (Gdx.input.isKeyPressed(Input.Keys.A)) velocity.x = -SPEED;
            if (Gdx.input.isKeyPressed(Input.Keys.D)) velocity.x = SPEED;
            if (Gdx.input.isKeyJustPressed(Input.Keys.W) && isGrounded) velocity.y = JUMP_FORCE;
        }

        float oldX = position.x;
        position.x += velocity.x * deltaTime;
        if (checkCollision()) position.x = oldX;

        velocity.y -= GRAVITY * deltaTime;
        float oldY = position.y;
        position.y += velocity.y * deltaTime;
        isGrounded = false;

        if (checkCollision()) {
            position.y = oldY;
            if (velocity.y < 0) isGrounded = true; 
            velocity.y = 0;
        }

        if (checkHazard("lav") && !isFireboy) isDead = true;
        if (checkHazard("su") && isFireboy) isDead = true;
        if (checkHazard("zehir")) isDead = true;
        if (position.y < -300) isDead = true; // Boşluğa düşme
    }

    private boolean checkCollision() {
        for (MapLayer layer : map.getLayers()) {
            if (layer instanceof TiledMapTileLayer) {
                TiledMapTileLayer tLayer = (TiledMapTileLayer) layer;
                if (tLayer.getName().toLowerCase().contains("back")) continue;
                
                if (isHittingTile(tLayer)) return true;
            }
        }
        return false;
    }

    private boolean checkHazard(String name) {
        for (MapLayer layer : map.getLayers()) {
            if (layer instanceof TiledMapTileLayer && layer.getName().toLowerCase().contains(name)) {
                if (isHittingTile((TiledMapTileLayer) layer)) return true;
            }
        }
        return false;
    }

    private boolean isHittingTile(TiledMapTileLayer layer) {
        int startX = (int) (position.x / layer.getTileWidth());
        int endX = (int) ((position.x + hitboxWidth) / layer.getTileWidth());
        int startY = (int) (position.y / layer.getTileHeight());
        int endY = (int) ((position.y + hitboxHeight) / layer.getTileHeight());

        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                if (layer.getCell(x, y) != null) return true;
            }
        }
        return false;
    }

    public void draw(SpriteBatch batch) {
        batch.draw(currentFrame, position.x - 84, position.y - 70, 192, 192);
    }

    public Rectangle getBounds() { return new Rectangle(position.x, position.y, hitboxWidth, hitboxHeight); }
    public boolean isDead() { return isDead; }
    public Vector2 getPosition() { return position; }
    public void dispose() { spriteSheet.dispose(); }
}