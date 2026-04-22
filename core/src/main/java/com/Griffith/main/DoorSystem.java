package com.Griffith.main;

import com.Griffith.Sprites.Player;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;

// This class is responsible for managing the door system in the game.
public class DoorSystem {

    private Rectangle finishZone;
    private MapLayer doorClosedLayer;
    private MapLayer doorOpenLayer;

    public void setFinishZone(Rectangle finishZone) {
        this.finishZone = finishZone;
    }

    // This method loads the door layers from the TiledMap and sets their initial
    // visibility.
    public void loadDoorLayers(TiledMap map) {
        doorClosedLayer = map.getLayers().get("door_closed");
        doorOpenLayer = map.getLayers().get("door_open");

        if (doorClosedLayer == null) {
            System.out.println("⚠️ door_closed layer not found!");
        }

        if (doorOpenLayer == null) {
            System.out.println("⚠️ door_open layer not found!");
        } else {
            doorOpenLayer.setVisible(false);
        }

        if (doorClosedLayer != null) {
            doorClosedLayer.setVisible(true);
        }
    }

    // This method checks if both players are at the finish zone and updates the
    // door layers accordingly.
    public String checkWin(Player player1, Player player2) {
        if (finishZone == null || player1 == null || player2 == null) {
            return null;
        }

        boolean player1AtDoor = player1.getBounds().overlaps(finishZone);
        boolean player2AtDoor = player2.getBounds().overlaps(finishZone);

        if (player1AtDoor && player2AtDoor) {
            if (doorClosedLayer != null) {
                doorClosedLayer.setVisible(false);
            }
            if (doorOpenLayer != null) {
                doorOpenLayer.setVisible(true);
            }

            player1.die(false);
            player2.die(false);
            return "YOU WIN! Press R to play again.";
        }

        return null;
    }

    // This method resets the door layers to their initial state.
    public void reset() {
        if (doorClosedLayer != null) {
            doorClosedLayer.setVisible(true);
        }
        if (doorOpenLayer != null) {
            doorOpenLayer.setVisible(false);
        }
    }
}
