package com.Griffith.main;

import com.Griffith.Sprites.Button;
import com.Griffith.Sprites.Coin;
import com.Griffith.Sprites.Hazard;
import com.Griffith.Sprites.Player;
import com.Griffith.audio.SoundManager;
import com.Griffith.audio.SoundType;
import com.Griffith.gameConstants.GameConstants;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
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

    private static final float BLOCK_PUSH_SOUND_INTERVAL = 0.18f;

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
    private final MovableBlockSystem movableBlocks = new MovableBlockSystem(0f, GameConstants.MAP_WIDTH);
    private MapLayer blockVisualLayer;

    // Hazards
    private final Hazard hazardSystem = new Hazard();

    // Door / finish
    private Rectangle finishZone;
    private MapLayer doorClosedLayer;
    private MapLayer doorOpenLayer;

    // Lift system
    private final Button buttonSystem = new Button();
    private final LifttDownSystem lifttDownSystem = new LifttDownSystem();
    private final Coin coinSystem = new Coin();
    private int pumpkinCoinCount = 0;
    private int docCoinCount = 0;

    // Game state
    private boolean gameOver = false;
    private boolean levelComplete = false;
    private boolean showCollisionDebug = false;
    private String message = "";
    private float blockPushSoundCooldown = 0f;
    private Main game;
    private final String mapPath;
    private final String levelCompleteMessage;
    private final boolean returnToMenuOnWin;

    public FirstScreen(Main game) {
        this(game, "maps/levelOne.tmx", "LEVEL 1 COMPLETE! Press ENTER for Level 2.", false);
    }

    protected FirstScreen(Main game, String mapPath, String levelCompleteMessage, boolean returnToMenuOnWin) {
        this.game = game;
        this.mapPath = mapPath;
        this.levelCompleteMessage = levelCompleteMessage;
        this.returnToMenuOnWin = returnToMenuOnWin;
    }

    // This method creates the screen resources and loads all map-driven gameplay
    // data.
    @Override
    public void show() {
        TmxMapLoader.Parameters params = new TmxMapLoader.Parameters();
        params.projectFilePath = "maps/tiled.tiled-project";
        map = new TmxMapLoader().load(mapPath, params);
        renderer = new OrthogonalTiledMapRenderer(map);

        camera = new OrthographicCamera();

        camera.setToOrtho(false, GameConstants.MAP_WIDTH, GameConstants.MAP_HEIGHT);

        batch = new SpriteBatch();
        font = new BitmapFont();
        debugRenderer = new ShapeRenderer();

        System.out.println("=== MAP LAYERS ===");
        for (MapLayer layer : map.getLayers()) {
            System.out.println("Layer: " + layer.getName());
        }
        System.out.println("==================");

        loadSpawnObjects();
        loadGround();
        loadBlockColliders();
        loadBlockVisualLayer();
        loadHazards();
        loadInteractions();
        loadLiftVisualLayer();
        lifttDownSystem.load(map, mapPath);
        loadDoorLayers();
        loadCoins();
    }

    // This method reads the player spawn points and the finish zone from the spawn
    // object layer.
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
                    finishZone = new Rectangle(x, y, width, height);
                }
            }
        } else {
            System.out.println("⚠️ Spawn layer 'spawn' not found!");
        }
    }

    // This method builds ground colliders from the map's ground tile layer.
    private void loadGround() {
        TiledMapTileLayer groundLayer = (TiledMapTileLayer) map.getLayers().get("ground");
        if (groundLayer != null) {
            float tileW = groundLayer.getTileWidth();
            float tileH = groundLayer.getTileHeight();

            for (int row = 0; row < groundLayer.getHeight(); row++) {
                for (int col = 0; col < groundLayer.getWidth(); col++) {
                    TiledMapTileLayer.Cell cell = groundLayer.getCell(col, row);
                    if (cell != null && cell.getTile() != null) {
                        addGroundColliderForCell(cell, col, row, tileW, tileH);
                    }
                }
            }
            System.out.println("Ground tiles loaded: " + groundTiles.size);
        } else {
            System.out.println("⚠️ Ground layer 'ground' not found!");
        }
    }

    private void addGroundColliderForCell(TiledMapTileLayer.Cell cell, int col, int row, float tileW, float tileH) {
        boolean addedCustomCollider = false;

        for (MapObject obj : cell.getTile().getObjects()) {
            if (obj instanceof RectangleMapObject) {
                Rectangle source = ((RectangleMapObject) obj).getRectangle();
                if (source.width <= 0f || source.height <= 0f) {
                    continue;
                }

                float localX = source.x;
                float localY = source.y;

                float colliderW = source.width * GameConstants.GROUND_WIDTH_SCALE;
                float colliderH = source.height * GameConstants.GROUND_HEIGHT_SCALE;
                float gameX = col * tileW + localX + GameConstants.GROUND_OFFSET_X + (source.width - colliderW) * 0.5f;
                float gameY = row * tileH + localY + GameConstants.GROUND_OFFSET_Y + (source.height - colliderH) * 0.5f;

                groundTiles.add(new Rectangle(gameX, gameY, colliderW, colliderH));
                addedCustomCollider = true;
            }
        }

        if (!addedCustomCollider) {
            float colliderW = tileW * GameConstants.GROUND_WIDTH_SCALE;
            float colliderH = tileH * GameConstants.GROUND_HEIGHT_SCALE;
            float gameX = col * tileW + GameConstants.GROUND_OFFSET_X + (tileW - colliderW) * 0.5f;
            float gameY = row * tileH + GameConstants.GROUND_OFFSET_Y + (tileH - colliderH) * 0.5f;
            groundTiles.add(new Rectangle(gameX, gameY, colliderW, colliderH));
        }
    }

    // This method loads pushable block colliders from the block object layer.
    private void loadBlockColliders() {
        MapLayer blockLayer = map.getLayers().get("block");

        if (blockLayer == null) {
            System.out.println("⚠️ block layer not found!");
            return;
        }

        movableBlocks.clear();

        System.out.println("Block object count: " + blockLayer.getObjects().getCount());

        for (MapObject obj : blockLayer.getObjects()) {
            if (obj instanceof RectangleMapObject) {
                Rectangle source = ((RectangleMapObject) obj).getRectangle();

                Rectangle rect = movableBlocks.addBlock(source);

                System.out.println("Loaded block rect -> x:" + rect.x +
                        " y:" + rect.y +
                        " w:" + rect.width +
                        " h:" + rect.height);
            }
        }

        System.out.println("Final block collider count: " + movableBlocks.getBlocks().size);
    }

    private void loadBlockVisualLayer() {
        blockVisualLayer = map.getLayers().get("block_visual");
        if (blockVisualLayer != null) {
            blockVisualLayer.setOffsetX(0f);
            blockVisualLayer.setOffsetY(0f);
            System.out.println("Block visual layer loaded.");
        }
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

    // This method locates the closed and open door layers and sets their initial
    // visibility.
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

    // This method runs the main frame update, rendering, HUD drawing, and optional
    // debug overlay.
    @Override
    public void render(float delta) {

        batch.setProjectionMatrix(camera.combined);

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MenuScreen(game));
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            resetGame();
        }

        if (levelComplete && Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            if (returnToMenuOnWin) {
                game.setScreen(new MenuScreen(game, true));
            } else {
                game.setScreen(new LevelTwoScreen(game));
            }
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F3)) {
            showCollisionDebug = !showCollisionDebug;
            System.out.println("showCollisionDebug = " + showCollisionDebug);
        }

        ScreenUtils.clear(Color.BLACK);
        camera.update();

        if (!gameOver && !levelComplete) {
            blockPushSoundCooldown = Math.max(0f, blockPushSoundCooldown - delta);
            Array<Rectangle> activeGround = new Array<>();
            activeGround.addAll(groundTiles);
            buttonSystem.addLiftParts(activeGround);
            buttonSystem.addButtonParts(activeGround);
            lifttDownSystem.addColliders(activeGround);

            if (player1 != null) {
                player1.update(delta, activeGround);
            }
            if (player2 != null) {
                player2.update(delta, activeGround);
            }

            updateMovableBlocks();
            resolvePlayersAgainstBlocks();
            buttonSystem.update(delta, player1, player2);
            lifttDownSystem.update(delta, player1, player2, buttonSystem);

            updateCoins();
            checkHazards();
            checkDoor();
        }

        AnimatedTiledMapTile.updateAnimationBaseTime();

        renderer.setView(camera);
        renderer.render();

        drawMovableBlocks();

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
        font.draw(batch, "P1: A/D/W | P2: Arrows | R: Reset | F3: Debug", 10, GameConstants.MAP_HEIGHT - 22);
        font.draw(batch, "Pumpkin Coins: " + pumpkinCoinCount + " | Blue Coins: " + docCoinCount, 10,
                GameConstants.MAP_HEIGHT - 6);

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
                if (!movableBlocks.getBlocks().contains(tile, true)) {
                    debugRenderer.rect(tile.x, tile.y, tile.width, tile.height);
                }
            }

            // block
            debugRenderer.setColor(Color.GREEN);
            for (Rectangle block : movableBlocks.getBlocks()) {
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

    // This method updates the per-player coin totals using the owner-specific coin
    // rules.
    private void updateCoins() {
        if (player1 != null) {
            pumpkinCoinCount += coinSystem.checkCollection(player1, Coin.CoinOwner.PUMPKIN);
        }

        if (player2 != null) {
            docCoinCount += coinSystem.checkCollection(player2, Coin.CoinOwner.DOC);
        }
    }

    // This method checks whether either player touched a hazard and records the
    // resulting loss message.
    private void checkHazards() {
        String hazardMessage = hazardSystem.checkHazards(player1, player2);
        if (hazardMessage != null) {
            gameOver = true;
            message = hazardMessage;
        }
    }

    // This method checks whether both players reached the finish zone and opens the
    // door on success.
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

            player1.die(false);
            player2.die(false);

            levelComplete = true;
            gameOver = true;
            message = levelCompleteMessage;
        }
    }

    // This method resets the lift system back to its initial state.
    private void resetLift() {
        buttonSystem.reset();
        lifttDownSystem.reset();
    }

    private void updateMovableBlocks() {
        pushBlocksForPlayer(player1);
        pushBlocksForPlayer(player2);
    }

    private void pushBlocksForPlayer(Player player) {
        if (player == null || player.isDead) {
            return;
        }

        float moveX = player.getLastMoveX();
        if (moveX == 0f) {
            return;
        }

        MovableBlockSystem.BlockPushResult result = movableBlocks.push(player.getBounds(), moveX, groundTiles);
        if (!result.moved()) {
            return;
        }

        Rectangle blockBeforeMove = new Rectangle(
                result.getBlock().x - result.getMoveX(),
                result.getBlock().y,
                result.getBlock().width,
                result.getBlock().height);
        carryPlayersStandingOnBlock(blockBeforeMove, result.getMoveX());
        playBlockPushSoundIfReady();
        applyBlockVisualOffset();
    }

    private void resolvePlayersAgainstBlocks() {
        resolvePlayerAgainstBlocks(player1);
        resolvePlayerAgainstBlocks(player2);
    }

    private void resolvePlayerAgainstBlocks(Player player) {
        if (player == null || player.isDead) {
            return;
        }

        Rectangle playerBounds = player.getBounds();
        for (Rectangle block : movableBlocks.getBlocks()) {
            if (!playerBounds.overlaps(block)) {
                continue;
            }

            if (movableBlocks.landsOnTop(playerBounds, player.velocityY, block)) {
                placePlayerOnTopOfBlock(player, block);
                playerBounds = player.getBounds();
                continue;
            }

            float moveX = player.getLastMoveX();
            if (moveX > 0f) {
                player.x = block.x - playerBounds.width;
            } else if (moveX < 0f) {
                player.x = block.x + block.width;
            } else if (playerBounds.x + playerBounds.width * 0.5f < block.x + block.width * 0.5f) {
                player.x = block.x - playerBounds.width;
            } else {
                player.x = block.x + block.width;
            }
            player.bounds.setPosition(player.x, player.y);
            playerBounds = player.getBounds();
        }
    }

    private void placePlayerOnTopOfBlock(Player player, Rectangle block) {
        player.y = block.y + block.height;
        player.velocityY = 0f;
        player.setOnGround(true);
        player.bounds.setPosition(player.x, player.y);
    }

    private void carryPlayersStandingOnBlock(Rectangle block, float moveX) {
        carryPlayerStandingOnBlock(player1, block, moveX);
        carryPlayerStandingOnBlock(player2, block, moveX);
    }

    private void carryPlayerStandingOnBlock(Player player, Rectangle block, float moveX) {
        if (player == null || player.isDead) {
            return;
        }

        Rectangle playerBounds = player.getBounds();
        if (movableBlocks.isStandingOnTop(playerBounds, block)) {
            player.moveBy(moveX, 0f);
        }
    }

    private void drawMovableBlocks() {
        if (movableBlocks.getBlocks().size == 0 || blockVisualLayer != null) {
            return;
        }

        Gdx.gl.glEnable(GL20.GL_BLEND);
        debugRenderer.setProjectionMatrix(camera.combined);
        debugRenderer.begin(ShapeRenderer.ShapeType.Filled);
        debugRenderer.setColor(Color.WHITE);
        for (Rectangle block : movableBlocks.getBlocks()) {
            debugRenderer.rect(block.x, block.y, block.width, block.height);
        }
        debugRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    // This method restores players, door state, lift state, and coin progress for a
    // fresh run.
    private void resetGame() {
        if (player1 != null) {
            player1.respawn();
        }
        if (player2 != null) {
            player2.respawn();
        }

        resetLift();
        resetBlocks();

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

    private void resetBlocks() {
        movableBlocks.reset();
        blockPushSoundCooldown = 0f;
        applyBlockVisualOffset();
    }

    private void applyBlockVisualOffset() {
        if (blockVisualLayer == null || movableBlocks.getBlocks().size == 0) {
            return;
        }

        blockVisualLayer.setOffsetX(movableBlocks.getVisualOffsetX());
        blockVisualLayer.setOffsetY(-movableBlocks.getVisualOffsetY());
    }

    // Plays a short scrape only when the white block actually moved this frame.
    private void playBlockPushSoundIfReady() {
        if (blockPushSoundCooldown > 0f) {
            return;
        }

        SoundManager.play(SoundType.BLOCK_PUSH, 0.45f);
        blockPushSoundCooldown = BLOCK_PUSH_SOUND_INTERVAL;
    }

    // This method updates the camera viewport when the window size changes.
    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, GameConstants.MAP_WIDTH, GameConstants.MAP_HEIGHT);
    }

    // This method is called when the application is paused.
    @Override
    public void pause() {
    }

    // This method is called when the application resumes from a paused state.
    @Override
    public void resume() {
    }

    // This method is called when the screen is no longer the active screen.
    @Override
    public void hide() {
    }

    // This method disposes all rendering resources and player/coin textures owned
    // by the screen.
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
