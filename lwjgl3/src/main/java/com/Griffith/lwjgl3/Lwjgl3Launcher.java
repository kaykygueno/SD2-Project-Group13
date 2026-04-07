package com.Griffith.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.Griffith.main.Main;

public class Lwjgl3Launcher {
    // This method starts the desktop application, restarting the JVM first when required by the platform.
    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired())
            return;
        createApplication();
    }

    // This method creates the libGDX desktop application with the main game class.
    private static Lwjgl3Application createApplication() {
        return new Lwjgl3Application(new Main(), getDefaultConfiguration());
    }

    // This method defines the default desktop window configuration for the launcher.
    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("FireBoy_WaterGirl");

        configuration.useVsync(true);

        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);

        configuration.setWindowedMode(640, 480);

        configuration.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");
        return configuration;
    }
}
