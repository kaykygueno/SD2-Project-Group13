package com.Griffith.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;

public class LevelThreeScreen extends FirstScreen {

    private Music music;

    public LevelThreeScreen(Main game) {
        super(game, "maps/levelThree.tmx", "LEVEL 3 COMPLETE! Press ENTER for menu.", true);
    }

    @Override
    public void show() {
        super.show();

        music = Gdx.audio.newMusic(Gdx.files.internal("sounds/level_soundtrack.wav"));
        music.setLooping(true);
        music.setVolume(0.6f);
        music.play();
    }

    @Override
    protected Screen getNextScreen() {
        return new MenuScreen(game, true);
    }

    @Override
    public void hide() {
        super.hide();

        if (music != null) {
            music.stop();
        }
    }

    @Override
    public void dispose() {
        super.dispose();

        if (music != null) {
            music.dispose();
        }
    }
}