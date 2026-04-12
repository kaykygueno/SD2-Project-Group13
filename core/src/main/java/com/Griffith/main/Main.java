package com.Griffith.main;

import com.badlogic.gdx.Game;
import com.Griffith.audio.SoundManager;

public class Main extends Game {

    // This method sets the game's initial screen when the application starts.
    @Override
    public void create() {
        // Initialize the sound manager
        SoundManager.init();

        // Angelo: sets the first screen to be the MenuScreen class
        setScreen(new MenuScreen(this));
    }

    // This method delegates disposal to the libGDX Game base class.
    @Override
    public void dispose() {
        // Dispose all sound resources
        SoundManager.dispose();
        super.dispose();
    }
}
