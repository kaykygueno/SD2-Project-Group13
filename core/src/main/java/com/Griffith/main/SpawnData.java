package com.Griffith.main;

import com.Griffith.Sprites.Player;
import com.badlogic.gdx.math.Rectangle;

// This class is used to store the spawn data for a level, including the player spawn points and the finish zone
public record SpawnData(Player player1, Player player2, Rectangle finishZone) {
}
