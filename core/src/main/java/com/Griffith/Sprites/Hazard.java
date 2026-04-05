package com.Griffith.Sprites;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

// This class is responsible for handling the hazard zones in the game, such as lava, water, and spikes
public class Hazard {

    private final Array<Rectangle> lavaZones = new Array<>();
    private final Array<Rectangle> waterZones = new Array<>();
    private final Array<Rectangle> spikeZones = new Array<>();

    public void loadHazards(TiledMap map) {
        lavaZones.clear();
        waterZones.clear();
        spikeZones.clear();

        MapLayer hazardLayer = map.getLayers().get("hazards");
        if (hazardLayer != null) {
            for (MapObject obj : hazardLayer.getObjects()) {
                if (obj instanceof RectangleMapObject) {
                    Rectangle source = ((RectangleMapObject) obj).getRectangle();
                    Rectangle rect = new Rectangle(source.x, source.y, source.width, source.height);

                    switch (obj.getName()) {
                        case "lava":
                            lavaZones.add(rect);
                            break;
                        case "water":
                            waterZones.add(rect);
                            break;
                        case "spikes":
                            spikeZones.add(rect);
                            break;
                    }
                }
            }
        } else {
            System.out.println("⚠️ Hazards layer not found!");
        }
    }

    public String checkHazards(Player player1, Player player2) {
        for (Rectangle water : waterZones) {
            if (player1 != null && !player1.isDead && player1.getBounds().overlaps(water)) {
                player1.die();
                return "Player1 fell into water! Press R to restart.";
            }
        }

        for (Rectangle spike : spikeZones) {
            if (player1 != null && !player1.isDead && player1.getBounds().overlaps(spike)) {
                player1.die();
                return "Player1 hit spikes! Press R to restart.";
            }
        }

        for (Rectangle lava : lavaZones) {
            if (player2 != null && !player2.isDead && player2.getBounds().overlaps(lava)) {
                player2.die();
                return "Player2 fell into lava! Press R to restart.";
            }
        }

        for (Rectangle spike : spikeZones) {
            if (player2 != null && !player2.isDead && player2.getBounds().overlaps(spike)) {
                player2.die();
                return "Player2 hit spikes! Press R to restart.";
            }
        }

        return null;
    }

    public Array<Rectangle> getLavaZones() {
        return lavaZones;
    }

    public Array<Rectangle> getWaterZones() {
        return waterZones;
    }

    public Array<Rectangle> getSpikeZones() {
        return spikeZones;
    }
}
