package com.Griffith.Tests;

import com.Griffith.gameConstants.GameConstants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestsFolderMarker {

    // Verifies the map dimensions still match the tile grid used by the level.
    @Test
    void mapSizeShouldMatchTileGrid() {
        assertEquals(480f, GameConstants.MAP_WIDTH);
        assertEquals(320f, GameConstants.MAP_HEIGHT);
    }

    // Ensures lift movement constants are positive so platform movement works.
    @Test
    void liftValuesShouldBePositive() {
        assertTrue(GameConstants.LIFT_SPEED > 0f);
        assertTrue(GameConstants.LIFT_TRAVEL_DISTANCE > 0f);
    }

    // Keeps collider scales in a valid range for collision rectangle generation.
    @Test
    void groundScalesShouldBeValid() {
        assertTrue(GameConstants.GROUND_WIDTH_SCALE > 0f);
        assertTrue(GameConstants.GROUND_HEIGHT_SCALE > 0f);
    }

    // Confirms default offsets are neutral unless intentionally changed.
    @Test
    void groundOffsetsShouldDefaultToZero() {
        assertEquals(0f, GameConstants.GROUND_OFFSET_X);
        assertEquals(0f, GameConstants.GROUND_OFFSET_Y);
    }
}
