package com.Griffith.main;

import com.badlogic.gdx.Game;

public class Main extends Game {

    // This method sets the game's initial screen when the application starts.
    @Override
    public void create() {
        // sets the first screen to be the FirstScreen class
        setScreen(new FirstScreen());
    }

    // This method delegates disposal to the libGDX Game base class.
    @Override
    public void dispose() {
        super.dispose();
    }
}
