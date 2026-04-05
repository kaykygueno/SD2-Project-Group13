package com.Griffith.main;

import com.badlogic.gdx.Game;

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