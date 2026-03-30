package com.Griffith;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
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
    private ShapeRenderer debugRenderer;

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
    private boolean showCollisionDebug = false;
    private String message = "";

    // Map dimensions: 30 tiles x 20 tiles at 16px each
    private static final float MAP_WIDTH = 30 * 16f; // 480
    private static final float MAP_HEIGHT = 20 * 16f; // 320

    // Ground collider tuning (adjust these if debug boxes look misaligned)
    private static final float GROUND_OFFSET_X = 0f;
    private static final float GROUND_OFFSET_Y = 0f;
    private static final float GROUND_WIDTH_SCALE = 1f;
    private static final float GROUND_HEIGHT_SCALE = 1f;

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
        debugRenderer = new ShapeRenderer();

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
                        // TiledMapTileLayer rows are already in world-space order for libGDX access.
                        float colliderW = tileW * GROUND_WIDTH_SCALE;
                        float colliderH = tileH * GROUND_HEIGHT_SCALE;
                        float gameX = col * tileW + GROUND_OFFSET_X + (tileW - colliderW) * 0.5f;
                        float gameY = row * tileH + GROUND_OFFSET_Y + (tileH - colliderH) * 0.5f;
                        groundTiles.add(new Rectangle(gameX, gameY, colliderW, colliderH));
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
                    Rectangle source = ((RectangleMapObject) obj).getRectangle();
                    // Keep hazard coordinates in the same object-space convention used by spawn
                    // objects.
                    Rectangle rect = new Rectangle(source.x, source.y, source.width, source.height);

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
        if (Gdx.input.isKeyJustPressed(Input.Keys.F3))
            showCollisionDebug = !showCollisionDebug;

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
        font.draw(batch, "Player1: A/D/W  |  Player2: Arrows  |  R: Restart  |  F3: Hitboxes", 10, 15);

        if (!message.isEmpty()) {
            font.setColor(Color.YELLOW);
            font.draw(batch, message, MAP_WIDTH / 2 - 120, MAP_HEIGHT / 2);
        }

        batch.end();

        if (showCollisionDebug) {
            debugRenderer.setProjectionMatrix(camera.combined);
            debugRenderer.begin(ShapeRenderer.ShapeType.Line);

            debugRenderer.setColor(Color.GRAY);
            for (Rectangle tile : groundTiles) {
                debugRenderer.rect(tile.x, tile.y, tile.width, tile.height);
            }

            debugRenderer.setColor(Color.RED);
            for (Rectangle lava : lavaZones) {
                debugRenderer.rect(lava.x, lava.y, lava.width, lava.height);
            }

            debugRenderer.setColor(Color.CYAN);
            for (Rectangle water : waterZones) {
                debugRenderer.rect(water.x, water.y, water.width, water.height);
            }

            debugRenderer.setColor(Color.YELLOW);
            for (Rectangle spikes : spikeZones) {
                debugRenderer.rect(spikes.x, spikes.y, spikes.width, spikes.height);
            }

            if (door != null) {
                debugRenderer.setColor(Color.LIME);
                debugRenderer.rect(door.x, door.y, door.width, door.height);
            }

            if (player1 != null) {
                debugRenderer.setColor(Color.ORANGE);
                Rectangle p1 = player1.getBounds();
                debugRenderer.rect(p1.x, p1.y, p1.width, p1.height);
            }

            if (player2 != null) {
                debugRenderer.setColor(Color.BLUE);
                Rectangle p2 = player2.getBounds();
                debugRenderer.rect(p2.x, p2.y, p2.width, p2.height);
            }

            debugRenderer.end();
        }
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
        debugRenderer.dispose();
        if (player1 != null)
            player1.dispose();
        if (player2 != null)
            player2.dispose();
    }
}