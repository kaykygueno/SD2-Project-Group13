package com.Griffith.main;

import com.badlogic.gdx.Screen;

public class LevelTwoScreen extends FirstScreen {

    public LevelTwoScreen(Main game) {
        super(game, "maps/levelOne.tmx", "LEVEL 2 COMPLETE! Press ENTER for menu.", false);
    }

    @Override
    protected Screen getNextScreen() {
        return new LevelThreeScreen(game);
    }
}
