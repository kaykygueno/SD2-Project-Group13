package com.Griffith.Sprites;

import com.Griffith.audio.SoundManager;
import com.Griffith.audio.SoundType;
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

    private final Array<Rectangle> coins = new Array<>();
    private final Array<Boolean> collected = new Array<>();
    private final Array<CoinOwner> owners = new Array<>();
    private final Array<Integer> visualCols = new Array<>();
    private final Array<Integer> visualRows = new Array<>();
    private final Array<TiledMapTileLayer.Cell> originalVisualCells = new Array<>();
    private TiledMapTileLayer coinVisualLayer;

    // Loads all coin hitboxes from the object layer and maps each one to a visual
    // tile so it can disappear when collected.
    public void loadCoins(TiledMap map) {
        coins.clear();
        collected.clear();
        owners.clear();
        visualCols.clear();
        visualRows.clear();
        originalVisualCells.clear();

        MapLayer coinLayer = map.getLayers().get("coins");
        coinVisualLayer = getCoinVisualLayer(map);

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
                storeCoinVisualPosition(r);
            }
        }

        System.out.println("Coins loaded: " + coins.size);
    }

    // Returns how many coins of the given owner were collected by the specified
    // player during this check.
    public int checkCollection(Player player, CoinOwner owner) {
        if (player == null) {
            return 0;
        }

        return checkCollection(player.getBounds(), owner);
    }

    // Returns how many coins of the given owner were collected by the specified
    // bounds during this check.
    public int checkCollection(Rectangle playerBounds, CoinOwner owner) {
        int collectedNow = 0;

        for (int i = 0; i < coins.size; i++) {
            if (!collected.get(i) && owners.get(i) == owner && playerBounds.overlaps(coins.get(i))) {
                collected.set(i, true);
                removeVisualCoin(i);
                collectedNow++;
            }
        }

        if (collectedNow > 0) {
            SoundManager.play(SoundType.COIN_COLLECT, 0.5f);
        }

        return collectedNow;
    }

    // Exposes the coin hitboxes for any external systems that need to inspect them.
    public Array<Rectangle> getCoins() {
        return coins;
    }

    // Exposes the collected-state list so other systems can inspect which coins are
    // already taken.
    public Array<Boolean> getCollected() {
        return collected;
    }

    // Marks every coin as uncollected so the level can be replayed from its initial
    // state.
    public void reset() {
        for (int i = 0; i < collected.size; i++) {
            collected.set(i, false);
            restoreVisualCoin(i);
        }
    }

    // Coin visuals are now tile-based (`coins_visual`), so sprite rendering is not
    // needed.
    public void draw(SpriteBatch batch) {
    }

    // No coin textures are allocated by this system.
    public void dispose() {
    }

    // Reads the owner property from a map object and defaults to the pumpkin player
    // if it is missing.
    private CoinOwner readOwner(MapObject obj) {
        String objectName = obj.getName();
        if (objectName != null) {
            if ("coins_blue".equalsIgnoreCase(objectName)) {
                return CoinOwner.DOC;
            }
            if ("coins_orange".equalsIgnoreCase(objectName)) {
                return CoinOwner.PUMPKIN;
            }
        }

        String owner = obj.getProperties().get("owner", String.class);
        if (owner == null) {
            return CoinOwner.PUMPKIN;
        }
        if ("blue".equalsIgnoreCase(owner) || "doc".equalsIgnoreCase(owner) || "player2".equalsIgnoreCase(owner)) {
            return CoinOwner.DOC;
        }
        return CoinOwner.PUMPKIN;
    }

    // Prefers the dedicated coin visual layer and falls back to the legacy map
    // naming.
    private TiledMapTileLayer getCoinVisualLayer(TiledMap map) {
        MapLayer coinsVisual = map.getLayers().get("coins_visual");
        if (coinsVisual instanceof TiledMapTileLayer) {
            return (TiledMapTileLayer) coinsVisual;
        }

        MapLayer legacySurface = map.getLayers().get("surface");
        if (legacySurface instanceof TiledMapTileLayer) {
            return (TiledMapTileLayer) legacySurface;
        }

        return null;
    }

    // Finds and stores the tile cell for this coin so it can be hidden/restored.
    private void storeCoinVisualPosition(Rectangle coinRect) {
        if (coinVisualLayer == null) {
            visualCols.add(-1);
            visualRows.add(-1);
            originalVisualCells.add(null);
            return;
        }

        int col = (int) ((coinRect.x + coinRect.width * 0.5f) / coinVisualLayer.getTileWidth());
        int row = (int) ((coinRect.y + coinRect.height * 0.5f) / coinVisualLayer.getTileHeight());

        visualCols.add(col);
        visualRows.add(row);

        if (col >= 0 && row >= 0) {
            originalVisualCells.add(coinVisualLayer.getCell(col, row));
        } else {
            originalVisualCells.add(null);
        }
    }

    // Removes the mapped coin tile when that coin gets collected.
    private void removeVisualCoin(int index) {
        if (coinVisualLayer == null) {
            return;
        }

        int col = visualCols.get(index);
        int row = visualRows.get(index);
        if (col >= 0 && row >= 0) {
            coinVisualLayer.setCell(col, row, null);
        }
    }

    // Restores the original coin tile for this index when resetting the level.
    private void restoreVisualCoin(int index) {
        if (coinVisualLayer == null) {
            return;
        }

        int col = visualCols.get(index);
        int row = visualRows.get(index);
        if (col >= 0 && row >= 0) {
            coinVisualLayer.setCell(col, row, originalVisualCells.get(index));
        }
    }
}
