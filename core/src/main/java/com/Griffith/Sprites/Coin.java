package com.Griffith.Sprites;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class Coin {

    private final Array<Rectangle> coins = new Array<>();
    private final Array<Boolean> collected = new Array<>();

    public void loadCoins(TiledMap map) {
        coins.clear();
        collected.clear();

        MapLayer coinLayer = map.getLayers().get("coins");

        if (coinLayer == null) {
            System.out.println("⚠️ coins layer not found!");
            return;
        }

        for (MapObject obj : coinLayer.getObjects()) {
            if (obj instanceof RectangleMapObject) {
                Rectangle r = ((RectangleMapObject) obj).getRectangle();
                coins.add(new Rectangle(r.x, r.y, r.width, r.height));
                collected.add(false);
            }
        }

        System.out.println("Coins loaded: " + coins.size);
    }

    public int checkCollection(Player player) {
        int collectedNow = 0;

        for (int i = 0; i < coins.size; i++) {
            if (!collected.get(i) && player.getBounds().overlaps(coins.get(i))) {
                collected.set(i, true);
                collectedNow++;
            }
        }

        return collectedNow;
    }

    public Array<Rectangle> getCoins() {
        return coins;
    }

    public Array<Boolean> getCollected() {
        return collected;
    }

    public void reset() {
        for (int i = 0; i < collected.size; i++) {
            collected.set(i, false);
        }
    }
}