package com.Griffith.main;

import com.badlogic.gdx.Game;
// The Main class is the entry point of the game.
public class Main extends Game {

    @Override
    public void create() {
        //Angelo: sets the first screen to be the MenuScreen class
        setScreen(new MenuScreen(this));
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}