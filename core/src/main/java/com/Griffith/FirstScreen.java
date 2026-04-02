package com.Griffith;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
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
    private GlyphLayout glyphLayout;

    // Players
    private Player player1;
    private Player player2;

    // Ground collision
    private Array<Rectangle> groundTiles = new Array<>();
    private Array<Rectangle> blockTiles = new Array<>();

    // Hazards
    private Array<Rectangle> lavaZones = new Array<>();
    private Array<Rectangle> waterZones = new Array<>();
    private Array<Rectangle> spikeZones = new Array<>();

    // Door / finish
    private Rectangle door;
    private Rectangle finishZone;
    private MapLayer doorClosedLayer;
    private MapLayer doorOpenLayer;

    // Lift system
    private Array<Rectangle> buttonRects = new Array<>();
    private Array<Rectangle> liftParts = new Array<>();
    private Array<Float> liftStartYs = new Array<>();
    private MapLayer liftVisualLayer;
    private boolean liftActive = false;
    private float lastLiftDeltaY = 0f;
    private float liftVisualOffsetY = 0f;

    private static final float LIFT_SPEED = 40f;
    private static final float LIFT_TRAVEL_DISTANCE = 70f;

    // Game state
    private boolean gameOver = false;
    private boolean levelComplete = false;
    private boolean showCollisionDebug = false;
    private String message = "";

    // Map dimensions
    private static final float MAP_WIDTH = 30 * 16f;
    private static final float MAP_HEIGHT = 20 * 16f;

    // Ground collider tuning
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
        glyphLayout = new GlyphLayout();

        System.out.println("=== MAP LAYERS ===");
        for (MapLayer layer : map.getLayers()) {
            System.out.println("Layer: " + layer.getName());
        }
        System.out.println("==================");

        loadSpawnObjects();
        loadGround();
        loadBlockColliders();
        loadHazards();
        loadInteractions();
        loadLiftVisualLayer();
        loadDoorLayers();
    }

    private void loadSpawnObjects() {
        MapLayer spawnLayer = map.getLayers().get("spawn");
        if (spawnLayer != null) {
            for (MapObject obj : spawnLayer.getObjects()) {
                float x = obj.getProperties().get("x", Float.class);
                float y = obj.getProperties().get("y", Float.class);
                float width = obj.getProperties().get("width", Float.class);
                float height = obj.getProperties().get("height", Float.class);

                if ("Player1".equals(obj.getName())) {
                    player1 = new Player(
                            x,
                            y,
                            "maps/images/Others/pumpkin_dudeCopy.png",
                            Input.Keys.A, Input.Keys.D, Input.Keys.W);
                }

                if ("Player2".equals(obj.getName())) {
                    player2 = new Player(
                            x,
                            y,
                            "maps/images/Others/docCopy.png",
                            Input.Keys.LEFT, Input.Keys.RIGHT, Input.Keys.UP);
                }

                if ("door".equals(obj.getName())) {
                    door = new Rectangle(x, y, width, height);
                    finishZone = new Rectangle(x, y, width, height);
                }
            }
        } else {
            System.out.println("⚠️ Spawn layer 'spawn' not found!");
        }
    }

    private void loadGround() {
        TiledMapTileLayer groundLayer = (TiledMapTileLayer) map.getLayers().get("groung");
        if (groundLayer != null) {
            float tileW = groundLayer.getTileWidth();
            float tileH = groundLayer.getTileHeight();

            for (int row = 0; row < groundLayer.getHeight(); row++) {
                for (int col = 0; col < groundLayer.getWidth(); col++) {
                    TiledMapTileLayer.Cell cell = groundLayer.getCell(col, row);
                    if (cell != null && cell.getTile() != null) {
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
    }

    private void loadBlockColliders() {
    MapLayer blockLayer = map.getLayers().get("block");

    if (blockLayer == null) {
        System.out.println("⚠️ block layer not found!");
        return;
    }

    blockTiles.clear();

    System.out.println("Block object count: " + blockLayer.getObjects().getCount());

    for (MapObject obj : blockLayer.getObjects()) {
        if (obj instanceof RectangleMapObject) {
            Rectangle source = ((RectangleMapObject) obj).getRectangle();

            Rectangle rect = new Rectangle(
                    source.x,
                    source.y,
                    source.width,
                    source.height
            );

            blockTiles.add(rect);
            groundTiles.add(rect);

            System.out.println("Loaded block rect -> x:" + rect.x +
                    " y:" + rect.y +
                    " w:" + rect.width +
                    " h:" + rect.height);
        }
    }

    System.out.println("Final block collider count: " + blockTiles.size);
}

    private void loadHazards() {
        MapLayer hazardLayer = map.getLayers().get("hazards");
        if (hazardLayer != null) {
            for (MapObject obj : hazardLayer.getObjects()) {
                if (obj instanceof RectangleMapObject) {
                    Rectangle source = ((RectangleMapObject) obj).getRectangle();
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
        } else {
            System.out.println("⚠️ Hazards layer not found!");
        }
    }

    private void loadInteractions() {
        MapLayer interactionLayer = map.getLayers().get("interactions");
        if (interactionLayer == null) {
            System.out.println("⚠️ interactions layer not found!");
            return;
        }

        buttonRects.clear();
        liftParts.clear();
        liftStartYs.clear();

        for (MapObject obj : interactionLayer.getObjects()) {
            if (obj instanceof RectangleMapObject) {
                Rectangle source = ((RectangleMapObject) obj).getRectangle();
                Rectangle rect = new Rectangle(source.x, source.y, source.width, source.height);

                if ("lever".equals(obj.getName())) {
                    buttonRects.add(rect);
                } else if ("plataform".equals(obj.getName())) {
                    liftParts.add(rect);
                    liftStartYs.add(rect.y);
                }
            }
        }

        System.out.println("Levers loaded: " + buttonRects.size);
        System.out.println("Lift parts loaded: " + liftParts.size);
    }

    private void loadLiftVisualLayer() {
        liftVisualLayer = map.getLayers().get("lift_visual");

        if (liftVisualLayer == null) {
            System.out.println("⚠️ lift_visual layer not found!");
        } else {
            liftVisualLayer.setOffsetY(0f);
            System.out.println("Lift visual layer loaded.");
        }
    }

    private void loadDoorLayers() {
        doorClosedLayer = map.getLayers().get("door_closed");
        doorOpenLayer = map.getLayers().get("door_open");

        if (doorClosedLayer == null) {
            System.out.println("⚠️ door_closed layer not found!");
        }

        if (doorOpenLayer == null) {
            System.out.println("⚠️ door_open layer not found!");
        } else {
            doorOpenLayer.setVisible(false);
        }

        if (doorClosedLayer != null) {
            doorClosedLayer.setVisible(true);
        }
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            resetGame();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F3)) {
            showCollisionDebug = !showCollisionDebug;
            System.out.println("showCollisionDebug = " + showCollisionDebug);
        }

        ScreenUtils.clear(Color.BLACK);
        camera.update();

        if (!gameOver && !levelComplete) {
            Array<Rectangle> activeGround = new Array<>();
            activeGround.addAll(groundTiles);
            activeGround.addAll(liftParts);

            if (player1 != null) {
                player1.update(delta, activeGround);
            }
            if (player2 != null) {
                player2.update(delta, activeGround);
            }

            updateLift(delta);

            if (player1 != null) {
                applyLiftCarry(player1);
            }
            if (player2 != null) {
                applyLiftCarry(player2);
            }

            checkHazards();
            checkDoor();
        }

        AnimatedTiledMapTile.updateAnimationBaseTime();

        renderer.setView(camera);
        renderer.render();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        if (player1 != null) {
            player1.draw(batch);
        }
        if (player2 != null) {
            player2.draw(batch);
        }

        font.setColor(Color.WHITE);
        font.draw(batch, "Player1: A/D/W | Player2: Arrows | R: Restart | F3: Debug", 10, 15);

        if (!message.isEmpty()) {
            font.setColor(Color.YELLOW);
            font.draw(batch, message, MAP_WIDTH / 2 - 120, MAP_HEIGHT / 2);
        }

        batch.end();

        if (showCollisionDebug) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            debugRenderer.setProjectionMatrix(camera.combined);
            debugRenderer.begin(ShapeRenderer.ShapeType.Line);

            // normal ground
            debugRenderer.setColor(Color.GRAY);
            for (Rectangle tile : groundTiles) {
                if (!blockTiles.contains(tile, true)) {
                    debugRenderer.rect(tile.x, tile.y, tile.width, tile.height);
                }
            }

            // block
            debugRenderer.setColor(Color.GREEN);
            for (Rectangle block : blockTiles) {
                debugRenderer.rect(block.x, block.y, block.width, block.height);
            }

            // lift
            debugRenderer.setColor(Color.ORANGE);
            for (Rectangle lift : liftParts) {
                debugRenderer.rect(lift.x, lift.y, lift.width, lift.height);
            }

            // buttons
            debugRenderer.setColor(Color.PINK);
            for (Rectangle button : buttonRects) {
                debugRenderer.rect(button.x, button.y, button.width, button.height);
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

            if (player1 != null) {
                debugRenderer.setColor(Color.WHITE);
                Rectangle p1 = player1.getBounds();
                debugRenderer.rect(p1.x, p1.y, p1.width, p1.height);
            }

            if (player2 != null) {
                debugRenderer.setColor(Color.BLUE);
                Rectangle p2 = player2.getBounds();
                debugRenderer.rect(p2.x, p2.y, p2.width, p2.height);
            }

            debugRenderer.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }
    }

    private void updateLift(float delta) {
        lastLiftDeltaY = 0f;

        if (buttonRects.size == 0 || liftParts.size == 0) {
            return;
        }

        boolean anyPlayerOnButton = false;

        for (Rectangle button : buttonRects) {
            boolean player1OnButton = player1 != null && player1.getBounds().overlaps(button);
            boolean player2OnButton = player2 != null && player2.getBounds().overlaps(button);

            if (player1OnButton || player2OnButton) {
                anyPlayerOnButton = true;
                break;
            }
        }

        liftActive = anyPlayerOnButton;

        float moveAmount = LIFT_SPEED * delta;

        if (liftActive) {
            float allowedMove = moveAmount;

            for (int i = 0; i < liftParts.size; i++) {
                float currentY = liftParts.get(i).y;
                float maxY = liftStartYs.get(i) + LIFT_TRAVEL_DISTANCE;
                float remaining = maxY - currentY;
                if (remaining < allowedMove) {
                    allowedMove = remaining;
                }
            }

            if (allowedMove > 0f) {
                for (Rectangle part : liftParts) {
                    part.y += allowedMove;
                }
                liftVisualOffsetY += allowedMove;
                applyLiftVisualOffset();
                lastLiftDeltaY = allowedMove;
            }
        } else {
            float allowedMove = moveAmount;

            for (int i = 0; i < liftParts.size; i++) {
                float currentY = liftParts.get(i).y;
                float minY = liftStartYs.get(i);
                float remaining = currentY - minY;
                if (remaining < allowedMove) {
                    allowedMove = remaining;
                }
            }

            if (allowedMove > 0f) {
                for (Rectangle part : liftParts) {
                    part.y -= allowedMove;
                }
                liftVisualOffsetY -= allowedMove;
                applyLiftVisualOffset();
                lastLiftDeltaY = -allowedMove;
            }
        }
    }

    private void applyLiftVisualOffset() {
        if (liftVisualLayer != null) {
            liftVisualLayer.setOffsetY(-liftVisualOffsetY);
        }
    }

    private void applyLiftCarry(Player player) {
        if (player == null || lastLiftDeltaY == 0f || liftParts.size == 0) {
            return;
        }

        Rectangle p = player.getBounds();

        for (Rectangle part : liftParts) {
            boolean standingOnTop =
                    p.x + p.width > part.x &&
                    p.x < part.x + part.width &&
                    Math.abs(p.y - (part.y + part.height)) < 4f &&
                    player.velocityY <= 0;

            if (standingOnTop) {
                player.moveBy(0f, lastLiftDeltaY);
                break;
            }
        }
    }

    private void checkHazards() {
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
        if (finishZone == null || player1 == null || player2 == null) {
            return;
        }

        boolean player1AtDoor = player1.getBounds().overlaps(finishZone);
        boolean player2AtDoor = player2.getBounds().overlaps(finishZone);

        if (player1AtDoor && player2AtDoor) {
            if (doorClosedLayer != null) {
                doorClosedLayer.setVisible(false);
            }
            if (doorOpenLayer != null) {
                doorOpenLayer.setVisible(true);
            }

            player1.die();
            player2.die();

            levelComplete = true;
            gameOver = true;
            message = "YOU WIN! Press R to play again.";
        }
    }

    private void resetLift() {
        for (int i = 0; i < liftParts.size; i++) {
            liftParts.get(i).y = liftStartYs.get(i);
        }

        liftVisualOffsetY = 0f;
        applyLiftVisualOffset();

        liftActive = false;
        lastLiftDeltaY = 0f;
    }

    private void resetGame() {
        if (player1 != null) {
            player1.respawn();
        }
        if (player2 != null) {
            player2.respawn();
        }

        resetLift();

        if (doorClosedLayer != null) {
            doorClosedLayer.setVisible(true);
        }
        if (doorOpenLayer != null) {
            doorOpenLayer.setVisible(false);
        }

        gameOver = false;
        levelComplete = false;
        message = "";
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, MAP_WIDTH, MAP_HEIGHT);
    }

    @Override
    public void pause() { }

    @Override
    public void resume() { }

    @Override
    public void hide() { }

    @Override
    public void dispose() {
        map.dispose();
        renderer.dispose();
        batch.dispose();
        font.dispose();
        debugRenderer.dispose();

        if (player1 != null) {
            player1.dispose();
        }
        if (player2 != null) {
            player2.dispose();
        }
    }
}