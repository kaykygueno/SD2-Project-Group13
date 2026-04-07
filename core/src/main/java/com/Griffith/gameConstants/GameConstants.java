package com.Griffith.gameConstants;

public final class GameConstants {

    public static final float MAP_WIDTH = 30 * 16f;
    public static final float MAP_HEIGHT = 20 * 18f;

    public static final float GROUND_OFFSET_X = 0f;
    public static final float GROUND_OFFSET_Y = 0f;
    public static final float GROUND_WIDTH_SCALE = 1f;
    public static final float GROUND_HEIGHT_SCALE = 1f;

    // This constructor is private because this class only stores shared constants.
    private GameConstants() {
    }
}
