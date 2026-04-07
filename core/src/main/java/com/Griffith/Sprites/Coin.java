package com.Griffith.Sprites;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class Coin {

    public enum CoinOwner {
        PUMPKIN,
        DOC
    }

    private final Texture pumpkinCoinTexture = new Texture("coin_orange.png");
    private final Texture docCoinTexture = new Texture("coin_blue.png");
    private final Array<Rectangle> coins = new Array<>();
    private final Array<Boolean> collected = new Array<>();
    private final Array<CoinOwner> owners = new Array<>();
    private TiledMapTileLayer coinVisualLayer;

    // Loads all coin hitboxes from the map, assigns owners, and clears any tile-layer visuals for those positions.
    public void loadCoins(TiledMap map) {
        coins.clear();
        collected.clear();
        owners.clear();

        MapLayer coinLayer = map.getLayers().get("coins");
        coinVisualLayer = (TiledMapTileLayer) map.getLayers().get("surface");

        if (coinLayer == null) {
            System.out.println("⚠️ coins layer not found!");
            return;
        }

        for (MapObject obj : coinLayer.getObjects()) {
            if (obj instanceof RectangleMapObject) {
                Rectangle r = ((RectangleMapObject) obj).getRectangle();
                coins.add(new Rectangle(r.x, r.y, r.width, r.height));
                collected.add(false);
                owners.add(readOwner(obj));
                clearCoinVisual(r);
            }
        }

        System.out.println("Coins loaded: " + coins.size);
    }

    // Returns how many coins of the given owner were collected by the specified player during this check.
    public int checkCollection(Player player, CoinOwner owner) {
        int collectedNow = 0;

        for (int i = 0; i < coins.size; i++) {
            if (!collected.get(i) && owners.get(i) == owner && player.getBounds().overlaps(coins.get(i))) {
                collected.set(i, true);
                collectedNow++;
            }
        }

        return collectedNow;
    }

    // Exposes the coin hitboxes for any external systems that need to inspect them.
    public Array<Rectangle> getCoins() {
        return coins;
    }

    // Exposes the collected-state list so other systems can inspect which coins are already taken.
    public Array<Boolean> getCollected() {
        return collected;
    }

    // Marks every coin as uncollected so the level can be replayed from its initial state.
    public void reset() {
        for (int i = 0; i < collected.size; i++) {
            collected.set(i, false);
        }
    }

    // Draws each remaining coin with the texture that matches its assigned owner.
    public void draw(SpriteBatch batch) {
        for (int i = 0; i < coins.size; i++) {
            if (!collected.get(i)) {
                Rectangle c = coins.get(i);
                batch.draw(getTexture(owners.get(i)), c.x, c.y, c.width, c.height);
            }
        }
    }

    // Releases the textures used by the two coin variants when the screen is disposed.
    public void dispose() {
        pumpkinCoinTexture.dispose();
        docCoinTexture.dispose();
    }

    // Reads the owner property from a map object and defaults to the pumpkin player if it is missing.
    private CoinOwner readOwner(MapObject obj) {
        String owner = obj.getProperties().get("owner", String.class);
        if (owner == null) {
            return CoinOwner.PUMPKIN;
        }
        if ("blue".equalsIgnoreCase(owner) || "doc".equalsIgnoreCase(owner) || "player2".equalsIgnoreCase(owner)) {
            return CoinOwner.DOC;
        }
        return CoinOwner.PUMPKIN;
    }

    // Selects the texture that should be used for the given coin owner.
    private Texture getTexture(CoinOwner owner) {
        return owner == CoinOwner.DOC ? docCoinTexture : pumpkinCoinTexture;
    }

    // Removes any tile-layer coin graphic at this coin position so only the runtime sprite is shown.
    private void clearCoinVisual(Rectangle coinRect) {
        if (coinVisualLayer == null) {
            return;
        }

        int col = (int) ((coinRect.x + coinRect.width * 0.5f) / coinVisualLayer.getTileWidth());
        int row = (int) ((coinRect.y + coinRect.height * 0.5f) / coinVisualLayer.getTileHeight());
        if (col >= 0 && row >= 0) {
            coinVisualLayer.setCell(col, row, null);
        }
    }
}
