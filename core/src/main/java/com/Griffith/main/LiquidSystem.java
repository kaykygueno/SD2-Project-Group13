package com.Griffith.main;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class LiquidSystem {

    public static boolean isInsideZone(Rectangle playerBounds, Array<Rectangle> zones) {
        if (playerBounds == null || zones == null) {
            return false;
        }

        for (Rectangle zone : zones) {
            if (playerBounds.overlaps(zone)) {
                return true;
            }
        }

        return false;
    }
}