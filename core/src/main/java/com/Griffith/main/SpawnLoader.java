package com.Griffith.main;

import com.Griffith.Sprites.Player;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;

// This class is responsible for loading the player spawn points and the finish zone from the TiledMap
public class SpawnLoader {

    public SpawnData load(TiledMap map) {
        Player player1 = null;
        Player player2 = null;
        Rectangle finishZone = null;

        MapLayer spawnLayer = map.getLayers().get("spawn");
        if (spawnLayer != null) {
            for (MapObject obj : spawnLayer.getObjects()) {
                float x = obj.getProperties().get("x", Float.class);
                float y = obj.getProperties().get("y", Float.class);
                float width = obj.getProperties().get("width", Float.class);
                float height = obj.getProperties().get("height", Float.class);

                if ("Player1".equals(obj.getName())) {
                    player1 = new Player(
                            x,
                            y,
                            "maps/images/Others/pumpkin_dudeCopy.png",
                            Input.Keys.A, Input.Keys.D, Input.Keys.W);
                }

                if ("Player2".equals(obj.getName())) {
                    player2 = new Player(
                            x,
                            y,
                            "maps/images/Others/docCopy.png",
                            Input.Keys.LEFT, Input.Keys.RIGHT, Input.Keys.UP);
                }

                if ("door".equals(obj.getName())) {
                    finishZone = new Rectangle(x, y, width, height);
                }
            }
        } else {
            System.out.println("⚠️ Spawn layer 'spawn' not found!");
        }

        return new SpawnData(player1, player2, finishZone);
    }
}
