package com.Griffith.main;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

// This class is used to store the collision data for a level, including the ground tiles and block tiles
public record CollisionData(Array<Rectangle> groundTiles, Array<Rectangle> blockTiles) {
}
