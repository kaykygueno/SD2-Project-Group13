package com.Griffith;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

public class FirstScreen implements Screen {
    private Main game;
    private SpriteBatch batch;
    private Player fireboy, watergirl;
    private OrthographicCamera camera;
    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;

    public FirstScreen(Main game) { this.game = game; }

    @Override
    public void show() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth() / 1.5f, Gdx.graphics.getHeight() / 1.5f);

        TmxMapLoader.Parameters params = new TmxMapLoader.Parameters();
        params.projectFilePath = "maps/tiled.tiled-project";
        map = new TmxMapLoader().load("maps/main.tmx", params);
        mapRenderer = new OrthogonalTiledMapRenderer(map);

        fireboy = new Player(64, 64, "maps/images/Tiny Swords (Free Pack)/Tiny Swords (Free Pack)/Units/Red Units/Pawn/Pawn_Idle.png", true, map);
        watergirl = new Player(120, 64, "maps/images/Tiny Swords (Free Pack)/Tiny Swords (Free Pack)/Units/Blue Units/Pawn/Pawn_Idle.png", false, map);
    }

    @Override
    public void render(float delta) {
        fireboy.update(delta);
        watergirl.update(delta);

        if (fireboy.isDead() || watergirl.isDead()) {
            game.setScreen(new MenuScreen(game, false));
            return;
        }

        if (fireboy.getPosition().x > 1500 && watergirl.getPosition().x > 1500) {
            game.setScreen(new MenuScreen(game, true));
            return;
        }

        float midX = (fireboy.getPosition().x + watergirl.getPosition().x) / 2f;
        float midY = (fireboy.getPosition().y + watergirl.getPosition().y) / 2f;
        camera.position.set(midX, midY, 0);
        camera.update();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        mapRenderer.setView(camera);
        mapRenderer.render();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        watergirl.draw(batch);
        fireboy.draw(batch);
        batch.end();
    }

    @Override public void resize(int width, int height) { 
        camera.viewportWidth = width/1.5f; 
        camera.viewportHeight = height/1.5f; 
    }
    
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    
    @Override public void dispose() { 
        batch.dispose(); 
        fireboy.dispose(); 
        watergirl.dispose(); 
        map.dispose(); 
        mapRenderer.dispose(); 
    }
}