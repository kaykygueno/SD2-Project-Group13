package com.Griffith.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class MenuScreen implements Screen {

    private static final int LEVEL_ONE = 1;
    private static final int LEVEL_TWO = 2;

    private final Main game;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final GlyphLayout layout;

    // Angelo: camera and viewport to handle different screen sizes and maintain aspect ratio
    private OrthographicCamera camera;
    private Viewport viewport;

    private final boolean win;
    private int selectedLevel = LEVEL_ONE;

    // Accessors for testing (made public so tests in other packages can access)
    public String getTitle() {
        return win ? "CONGRATULATIONS!" : "FIRE AND WATER";
    }

    public String getSubtitle() {
        return win ? "SELECT A LEVEL TO PLAY AGAIN" : "SELECT A LEVEL";
    }

    public String getLevelOneOption() {
        return "LEVEL 1 - ORIGINAL MAP";
    }

    public String getLevelTwoOption() {
        return "LEVEL 2 - CRYSTAL CAVERN";
    }

    // Public constructor primarily for tests that avoids libGDX initialization
    public MenuScreen(boolean winOnly) {
        this.game = null;
        this.win = winOnly;
        this.batch = null;
        this.font = null;
        this.layout = null;
    }

    // This constructor initializes the menu screen and configures whether it should show the win message.
    public MenuScreen(Main game, boolean win) {
        this.game = game;
        this.win = win;

        batch = new SpriteBatch();
        font = new BitmapFont();
        layout = new GlyphLayout();

        font.getData().setScale(2f);
    }

    // This constructor creates the default non-win version of the menu screen.
    public MenuScreen(Main game) {
        this(game, false);
    }

    // This method draws the menu text and starts the selected gameplay screen.
    @Override
    public void show() {
        camera = new OrthographicCamera();

        //Angelo: FitViewport keeps the aspect ratio consistent across different screen sizes by adding black bars if necessary. The virtual size is set to 800x600, which is a common resolution for games.
        viewport = new FitViewport(800, 600, camera); // resolução base
        viewport.apply();
    }

    // This method is called when the menu screen becomes active.
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(camera.combined);

        handleMenuInput();

        String title = getTitle();
        String subtitle = getSubtitle();
        String instructions = "UP/DOWN + ENTER OR PRESS 1/2";

        // centralização com viewport
        layout.setText(font, title);
        float titleX = (viewport.getWorldWidth() - layout.width) / 2f;
        float titleY = viewport.getWorldHeight() / 2f + 95;

        layout.setText(font, subtitle);
        float subX = (viewport.getWorldWidth() - layout.width) / 2f;
        float subY = viewport.getWorldHeight() / 2f + 45;

        batch.begin();
        font.setColor(1f, 1f, 1f, 1f);
        font.draw(batch, title, titleX, titleY);
        font.draw(batch, subtitle, subX, subY);
        drawLevelOption(getLevelOneOption(), LEVEL_ONE, viewport.getWorldHeight() / 2f - 15);
        drawLevelOption(getLevelTwoOption(), LEVEL_TWO, viewport.getWorldHeight() / 2f - 60);

        font.setColor(0.75f, 0.85f, 1f, 1f);
        layout.setText(font, instructions);
        font.draw(batch, instructions, (viewport.getWorldWidth() - layout.width) / 2f, viewport.getWorldHeight() / 2f - 120);
        font.setColor(1f, 1f, 1f, 1f);
        batch.end();
    }

    private void drawLevelOption(String text, int level, float y) {
        String line = (selectedLevel == level ? "> " : "  ") + text;
        layout.setText(font, line);
        float x = (viewport.getWorldWidth() - layout.width) / 2f;

        if (selectedLevel == level) {
            font.setColor(1f, 0.86f, 0.24f, 1f);
        } else {
            font.setColor(0.9f, 0.9f, 0.9f, 1f);
        }

        font.draw(batch, line, x, y);
    }

    private void handleMenuInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            selectedLevel = selectedLevel == LEVEL_ONE ? LEVEL_TWO : LEVEL_ONE;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            selectedLevel = selectedLevel == LEVEL_ONE ? LEVEL_TWO : LEVEL_ONE;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_1)) {
            selectedLevel = LEVEL_ONE;
            startSelectedLevel();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2) || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_2)) {
            selectedLevel = LEVEL_TWO;
            startSelectedLevel();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            startSelectedLevel();
        }
    }

    private void startSelectedLevel() {
        if (selectedLevel == LEVEL_TWO) {
            game.setScreen(new LevelTwoScreen(game));
        } else {
            game.setScreen(new FirstScreen(game));
        }
    }

    // This method is called when the menu screen viewport changes size.
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true); // 🔥 ISSO FAZ FUNCIONAR
    }

    // This method is called when the application is paused on the menu screen.
    @Override
    public void pause() {
    }

    // This method is called when the application resumes while the menu screen is active.
    @Override
    public void resume() {
    }

    // This method is called when the menu screen is hidden.
    @Override
    public void hide() {
    }

    // This method disposes the menu rendering resources.
    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (font != null) font.dispose();
    }
}
