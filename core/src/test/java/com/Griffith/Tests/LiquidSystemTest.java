package com.Griffith.main;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LiquidSystemTest {

    // Tests for the isInsideZone method in the LiquidSystem class
    @Test
    void playerInsideUnderwaterZoneReturnsTrue() {
        Rectangle player = new Rectangle(10, 10, 16, 16);

        Array<Rectangle> underwaterZones = new Array<>();
        underwaterZones.add(new Rectangle(0, 0, 50, 50));

        assertTrue(LiquidSystem.isInsideZone(player, underwaterZones));
    }

    @Test
    void playerOutsideUnderwaterZoneReturnsFalse() {
        Rectangle player = new Rectangle(100, 100, 16, 16);

        Array<Rectangle> underwaterZones = new Array<>();
        underwaterZones.add(new Rectangle(0, 0, 50, 50));

        assertFalse(LiquidSystem.isInsideZone(player, underwaterZones));
    }

    @Test
    void nullZoneListReturnsFalse() {
        Rectangle player = new Rectangle(10, 10, 16, 16);

        assertFalse(LiquidSystem.isInsideZone(player, null));
    }
}