package com.Griffith.main;

import com.badlogic.gdx.Game;

// The Main class is the entry point of the game.
public class Main extends Game {

    @Override
    public void create() {
        // sets the first screen to be the FirstScreen class
        setScreen(new FirstScreen());
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}