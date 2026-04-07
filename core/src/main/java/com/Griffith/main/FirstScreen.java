package com.Griffith.main;

import com.Griffith.Sprites.Button;
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
import com.badlogic.gdx.maps.tiled.TiledMap;
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

    // Hazards and interactions
    private final Hazard hazardSystem = new Hazard();
    private final Button buttonSystem = new Button();
    private final SpawnLoader spawnLoader = new SpawnLoader();
    private final CollisionLoader collisionLoader = new CollisionLoader();
    private final DoorSystem doorSystem = new DoorSystem();

    // Game state
    private boolean gameOver = false;
    private boolean levelComplete = false;
    private boolean showCollisionDebug = false;
    private String message = "";
    private Main game;

    public FirstScreen(Main game) {
        this.game = game;
    }

    // Map dimensions
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

        loadSpawns();
        loadCollisions();
        loadHazards();
        loadInteractions();
        loadLiftVisualLayer();
        doorSystem.loadDoorLayers(map);
    }

    // This method loads the player spawn points and the finish zone from the
    // TiledMap
    private void loadSpawns() {
        SpawnData spawnData = spawnLoader.load(map);
        player1 = spawnData.player1();
        player2 = spawnData.player2();
        doorSystem.setFinishZone(spawnData.finishZone());
    }

    // This method loads the collision data from the TiledMap
    private void loadCollisions() {
        CollisionData collisionData = collisionLoader.load(map);
        groundTiles = collisionData.groundTiles();
        blockTiles = collisionData.blockTiles();
    }

    // This method loads the hazard zones from the TiledMap
    private void loadHazards() {
        hazardSystem.loadHazards(map);
    }

    // This method loads the interactive elements from the TiledMap

    private void loadInteractions() {
        buttonSystem.loadInteractions(map);
    }

    // This method loads the lift visual layer from the TiledMap
    private void loadLiftVisualLayer() {
        buttonSystem.loadLiftVisualLayer(map);
    }

    // This method is called every frame to update the game logic
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

            debugRenderer.setColor(Color.RED);
            for (Rectangle lava : hazardSystem.getLavaZones()) {
                debugRenderer.rect(lava.x, lava.y, lava.width, lava.height);
            }

            debugRenderer.setColor(Color.CYAN);
            for (Rectangle water : hazardSystem.getWaterZones()) {
                debugRenderer.rect(water.x, water.y, water.width, water.height);
            }

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

    private void checkHazards() {
        String hazardMessage = hazardSystem.checkHazards(player1, player2);
        if (hazardMessage != null) {
            gameOver = true;
            message = hazardMessage;
        }
    }

    private void checkDoor() {
        String winMessage = doorSystem.checkWin(player1, player2);
        if (winMessage != null) {
            levelComplete = true;
            gameOver = true;
            message = winMessage;
        }
    }

    private void resetLift() {
        buttonSystem.reset();
    }

    private void resetGame() {
        if (player1 != null) {
            player1.respawn();
        }
        if (player2 != null) {
            player2.respawn();
        }

        resetLift();

        doorSystem.reset();

        gameOver = false;
        levelComplete = false;
        message = "";
    }
            
    @Override //Angelo: This method is called when the screen is resized, and it updates the camera's viewport to match the new screen size.
    public void resize(int width, int height) {
        camera.setToOrtho(false, GameConstants.MAP_WIDTH, GameConstants.MAP_HEIGHT);
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

        if (player1 != null) {
            player1.dispose();
        }
        if (player2 != null) {
            player2.dispose();
        }
    }
}