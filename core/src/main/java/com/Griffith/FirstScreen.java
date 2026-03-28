package com.Griffith;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

public class FirstScreen implements Screen {

    // Map
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private OrthographicCamera camera;

    // Rendering
    private SpriteBatch batch;
    private BitmapFont font;

    // Players
    private Player player1;
    private Player player2;

    // Ground collision
    private Array<Rectangle> groundTiles = new Array<>();

    // Hazards
    private Array<Rectangle> lavaZones = new Array<>();
    private Array<Rectangle> waterZones = new Array<>();
    private Array<Rectangle> spikeZones = new Array<>();

    // Door
    private Rectangle door;

    // Game state
    private boolean gameOver = false;
    private boolean levelComplete = false;
    private String message = "";

    // Map dimensions: 30 tiles x 20 tiles at 16px each
    private static final float MAP_WIDTH = 30 * 16f; // 480
    private static final float MAP_HEIGHT = 20 * 16f; // 320

    @Override
    public void show() {
        TmxMapLoader.Parameters params = new TmxMapLoader.Parameters();
        params.projectFilePath = "maps/tiled.tiled-project";
        map = new TmxMapLoader().load("maps/main.tmx", params);
        renderer = new OrthogonalTiledMapRenderer(map);

        camera = new OrthographicCamera();
        camera.setToOrtho(false, MAP_WIDTH, MAP_HEIGHT);

        batch = new SpriteBatch();
        font = new BitmapFont();

        MapLayer spawnLayer = map.getLayers().get("spawn");
        if (spawnLayer != null) {
            for (MapObject obj : spawnLayer.getObjects()) {
                float x = obj.getProperties().get("x", Float.class);
                float y = obj.getProperties().get("y", Float.class);
                float width = obj.getProperties().get("width", Float.class);
                float height = obj.getProperties().get("height", Float.class);
                float convertedY = y;

                if (obj.getName().equals("Player1")) {
                    player1 = new Player(
                            x,
                            convertedY,
                            "maps/images/Others/pumpkin_dudeCopy.png",
                            Input.Keys.A, Input.Keys.D, Input.Keys.W);
                    System.out.println("Player1 spawned at: " + x + ", " + convertedY);
                }

                if (obj.getName().equals("Player2")) {
                    player2 = new Player(
                            x,
                            convertedY,
                            "maps/images/Others/docCopy.png",
                            Input.Keys.LEFT, Input.Keys.RIGHT, Input.Keys.UP);
                    System.out.println("Player2 spawned at: " + x + ", " + convertedY);
                }

                if (obj.getName().equals("door")) {
                    float doorHeight = height;
                    float doorWidth = width;
                    float doorY = y;
                    door = new Rectangle(x, doorY, doorWidth, doorHeight);
                    System.out.println("Door at: " + x + ", " + doorY);
                }
            }
        } else {
            System.out.println("⚠️ Spawn layer 'spawn' not found!");
        }

        TiledMapTileLayer groundLayer = (TiledMapTileLayer) map.getLayers().get("groung");
        if (groundLayer != null) {
            float tileW = groundLayer.getTileWidth();
            float tileH = groundLayer.getTileHeight();
            for (int row = 0; row < groundLayer.getHeight(); row++) {
                for (int col = 0; col < groundLayer.getWidth(); col++) {
                    TiledMapTileLayer.Cell cell = groundLayer.getCell(col, row);
                    if (cell != null && cell.getTile() != null) {
                        // Convert Tiled Y (top-down) to game Y (bottom-up) and keep full tile collider.
                        float gameY = (groundLayer.getHeight() - row - 1) * tileH;
                        groundTiles.add(new Rectangle(col * tileW, gameY, tileW, tileH));
                    }
                }
            }
            System.out.println("Ground tiles loaded: " + groundTiles.size);
        } else {
            System.out.println("⚠️ Ground layer 'groung' not found!");
        }

        MapLayer hazardLayer = map.getLayers().get("hazards");
        if (hazardLayer != null) {
            for (MapObject obj : hazardLayer.getObjects()) {
                if (obj instanceof RectangleMapObject) {
                    Rectangle rect = ((RectangleMapObject) obj).getRectangle();
                    rect.y = MAP_HEIGHT - rect.y - rect.height;

                    switch (obj.getName()) {
                        case "lava":
                            lavaZones.add(rect);
                            break;
                        case "water":
                            waterZones.add(rect);
                            break;
                        case "spikes":
                            spikeZones.add(rect);
                            break;
                    }
                }
            }

            System.out.println("Hazards loaded — lava: " + lavaZones.size
                    + ", water: " + waterZones.size
                    + ", spikes: " + spikeZones.size);
        } else {
            System.out.println("⚠️ Hazards layer not found!");
        }
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.R))
            resetGame();

        ScreenUtils.clear(Color.BLACK);

        camera.update();
        renderer.setView(camera);
        renderer.render();

        if (!gameOver && !levelComplete) {
            if (player1 != null)
                player1.update(delta, groundTiles);
            if (player2 != null)
                player2.update(delta, groundTiles);

            checkHazards();
            checkDoor();
        }

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        if (player1 != null)
            player1.draw(batch);
        if (player2 != null)
            player2.draw(batch);

        font.setColor(Color.WHITE);
        font.draw(batch, "Player1: A/D/W  |  Player2: Arrows  |  R: Restart", 10, 15);

        if (!message.isEmpty()) {
            font.setColor(Color.YELLOW);
            font.draw(batch, message, MAP_WIDTH / 2 - 120, MAP_HEIGHT / 2);
        }

        batch.end();
    }

    private void checkHazards() {
        // Player1 dies in water and spikes
        for (Rectangle water : waterZones) {
            if (player1 != null && !player1.isDead && player1.getBounds().overlaps(water)) {
                player1.die();
                gameOver = true;
                message = "Player1 fell into water! Press R to restart.";
            }
        }
        for (Rectangle spike : spikeZones) {
            if (player1 != null && !player1.isDead && player1.getBounds().overlaps(spike)) {
                player1.die();
                gameOver = true;
                message = "Player1 hit spikes! Press R to restart.";
            }
        }

        // Player2 dies in lava and spikes
        for (Rectangle lava : lavaZones) {
            if (player2 != null && !player2.isDead && player2.getBounds().overlaps(lava)) {
                player2.die();
                gameOver = true;
                message = "Player2 fell into lava! Press R to restart.";
            }
        }
        for (Rectangle spike : spikeZones) {
            if (player2 != null && !player2.isDead && player2.getBounds().overlaps(spike)) {
                player2.die();
                gameOver = true;
                message = "Player2 hit spikes! Press R to restart.";
            }
        }
    }

    private void checkDoor() {
        if (door == null || player1 == null || player2 == null)
            return;
        if (player1.getBounds().overlaps(door) && player2.getBounds().overlaps(door)) {
            levelComplete = true;
            message = "Level Complete! Press R to play again.";
        }
    }

    private void resetGame() {
        if (player1 != null)
            player1.respawn();
        if (player2 != null)
            player2.respawn();
        gameOver = false;
        levelComplete = false;
        message = "";
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, MAP_WIDTH, MAP_HEIGHT);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        map.dispose();
        renderer.dispose();
        batch.dispose();
        font.dispose();
        if (player1 != null)
            player1.dispose();
        if (player2 != null)
            player2.dispose();
    }
}