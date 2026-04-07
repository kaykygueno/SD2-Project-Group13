package com.Griffith.main;

import com.Griffith.Sprites.Button;
import com.Griffith.Sprites.Coin;
import com.Griffith.Sprites.Hazard;
import com.Griffith.Sprites.Player;
import com.Griffith.gameConstants.GameConstants;
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
    private final Hazard hazardSystem = new Hazard();

    // Door / finish
    private Rectangle door;
    private Rectangle finishZone;
    private MapLayer doorClosedLayer;
    private MapLayer doorOpenLayer;

    // Lift system
    private final Button buttonSystem = new Button();
    private final Coin coinSystem = new Coin();
    private int pumpkinCoinCount = 0;
    private int docCoinCount = 0;

    // Game state
    private boolean gameOver = false;
    private boolean levelComplete = false;
    private boolean showCollisionDebug = false;
    private String message = "";

    // This method creates the screen resources and loads all map-driven gameplay data.
    @Override
    public void show() {
        TmxMapLoader.Parameters params = new TmxMapLoader.Parameters();
        params.projectFilePath = "maps/tiled.tiled-project";
        map = new TmxMapLoader().load("maps/main.tmx", params);
        renderer = new OrthogonalTiledMapRenderer(map);

        camera = new OrthographicCamera();
        camera.setToOrtho(false, GameConstants.MAP_WIDTH, GameConstants.MAP_HEIGHT);

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
        loadCoins();
    }

    // This method reads the player spawn points and the finish zone from the spawn object layer.
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

    // This method builds ground colliders from the map's ground tile layer.
    private void loadGround() {
        TiledMapTileLayer groundLayer = (TiledMapTileLayer) map.getLayers().get("groung");
        if (groundLayer != null) {
            float tileW = groundLayer.getTileWidth();
            float tileH = groundLayer.getTileHeight();

            for (int row = 0; row < groundLayer.getHeight(); row++) {
                for (int col = 0; col < groundLayer.getWidth(); col++) {
                    TiledMapTileLayer.Cell cell = groundLayer.getCell(col, row);
                    if (cell != null && cell.getTile() != null) {
                        float colliderW = tileW * GameConstants.GROUND_WIDTH_SCALE;
                        float colliderH = tileH * GameConstants.GROUND_HEIGHT_SCALE;
                        float gameX = col * tileW + GameConstants.GROUND_OFFSET_X + (tileW - colliderW) * 0.5f;
                        float gameY = row * tileH + GameConstants.GROUND_OFFSET_Y + (tileH - colliderH) * 0.5f;
                        groundTiles.add(new Rectangle(gameX, gameY, colliderW, colliderH));
                    }
                }
            }
            System.out.println("Ground tiles loaded: " + groundTiles.size);
        } else {
            System.out.println("⚠️ Ground layer 'groung' not found!");
        }
    }

    // This method loads extra block colliders from the block object layer and adds them to the ground set.
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

    // This method delegates hazard loading to the hazard system.
    private void loadHazards() {
        hazardSystem.loadHazards(map);
    }

    // This method delegates button and lift collider loading to the button system.
    private void loadInteractions() {
        buttonSystem.loadInteractions(map);
    }

    // This method delegates lift visual layer loading to the button system.
    private void loadLiftVisualLayer() {
        buttonSystem.loadLiftVisualLayer(map);
    }

    // This method locates the closed and open door layers and sets their initial visibility.
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

    // This method delegates coin hitbox loading to the coin system.
    private void loadCoins() {
        coinSystem.loadCoins(map);
    }

    // This method runs the main frame update, rendering, HUD drawing, and optional debug overlay.
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
            buttonSystem.addLiftParts(activeGround);

            if (player1 != null) {
                player1.update(delta, activeGround);
            }
            if (player2 != null) {
                player2.update(delta, activeGround);
            }

            buttonSystem.update(delta, player1, player2);

            updateCoins();
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

        coinSystem.draw(batch);

        font.setColor(Color.WHITE);
        font.draw(batch, "Player1: A/D/W | Player2: Arrows | R: Restart | F3: Debug", 10, 15);
        font.draw(batch, "Pumpkin Coins: " + pumpkinCoinCount + " | Blue Coins: " + docCoinCount, 10, 35);

        if (!message.isEmpty()) {
            font.setColor(Color.YELLOW);
            font.draw(batch, message, GameConstants.MAP_WIDTH / 2 - 120, GameConstants.MAP_HEIGHT / 2);
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
            for (Rectangle lift : buttonSystem.getLiftParts()) {
                debugRenderer.rect(lift.x, lift.y, lift.width, lift.height);
            }

            // buttons
            debugRenderer.setColor(Color.PINK);
            for (Rectangle button : buttonSystem.getButtonRects()) {
                debugRenderer.rect(button.x, button.y, button.width, button.height);
            }

            // lava
            debugRenderer.setColor(Color.RED);
            for (Rectangle lava : hazardSystem.getLavaZones()) {
                debugRenderer.rect(lava.x, lava.y, lava.width, lava.height);
            }

            // water
            debugRenderer.setColor(Color.CYAN);
            for (Rectangle water : hazardSystem.getWaterZones()) {
                debugRenderer.rect(water.x, water.y, water.width, water.height);
            }

            // spikes
            debugRenderer.setColor(Color.YELLOW);
            for (Rectangle spikes : hazardSystem.getSpikeZones()) {
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

    // This method updates the per-player coin totals using the owner-specific coin rules.
    private void updateCoins() {
        if (player1 != null) {
            pumpkinCoinCount += coinSystem.checkCollection(player1, Coin.CoinOwner.PUMPKIN);
        }

        if (player2 != null) {
            docCoinCount += coinSystem.checkCollection(player2, Coin.CoinOwner.DOC);
        }
    }

    // This method checks whether either player touched a hazard and records the resulting loss message.
    private void checkHazards() {
        String hazardMessage = hazardSystem.checkHazards(player1, player2);
        if (hazardMessage != null) {
            gameOver = true;
            message = hazardMessage;
        }
    }

    // This method checks whether both players reached the finish zone and opens the door on success.
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

    // This method resets the lift system back to its initial state.
    private void resetLift() {
        buttonSystem.reset();
    }

    // This method restores players, door state, lift state, and coin progress for a fresh run.
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
        coinSystem.reset();
        pumpkinCoinCount = 0;
        docCoinCount = 0;
    }

    // This method updates the camera viewport when the window size changes.
    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, GameConstants.MAP_WIDTH, GameConstants.MAP_HEIGHT);
    }

    // This method is called when the application is paused.
    @Override
    public void pause() { }

    // This method is called when the application resumes from a paused state.
    @Override
    public void resume() { }

    // This method is called when the screen is no longer the active screen.
    @Override
    public void hide() { }

    // This method disposes all rendering resources and player/coin textures owned by the screen.
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
        coinSystem.dispose();
    }
}
