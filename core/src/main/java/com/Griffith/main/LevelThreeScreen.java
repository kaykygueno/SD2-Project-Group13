package com.Griffith.main;

import com.badlogic.gdx.Screen;

public class LevelThreeScreen extends FirstScreen {

    public LevelThreeScreen(Main game) {
        super(game, "maps/levelThree.tmx", "LEVEL 3 COMPLETE! Press ENTER for menu.", true);
    }

    @Override
    protected Screen getNextScreen() {
    return new MenuScreen(game, true);
}
}
