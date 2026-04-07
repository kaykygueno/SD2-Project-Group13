package com.Griffith.main;

import com.Griffith.gameConstants.GameConstants;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

// This class is responsible for loading the collision data from the TiledMap 
public class CollisionLoader {

    // This method loads the collision data from the TiledMap
    public CollisionData load(TiledMap map) {
        Array<Rectangle> groundTiles = new Array<>();
        Array<Rectangle> blockTiles = new Array<>();

        loadGround(map, groundTiles);
        loadBlocks(map, groundTiles, blockTiles);

        return new CollisionData(groundTiles, blockTiles);
    }

    // This method loads the ground tiles from the "ground" layer of the TiledMap
    // and creates colliders for them
    private void loadGround(TiledMap map, Array<Rectangle> groundTiles) {
        TiledMapTileLayer groundLayer = (TiledMapTileLayer) map.getLayers().get("ground");
        if (groundLayer != null) {
            float tileW = groundLayer.getTileWidth();
            float tileH = groundLayer.getTileHeight();

            for (int row = 0; row < groundLayer.getHeight(); row++) {
                for (int col = 0; col < groundLayer.getWidth(); col++) {
                    TiledMapTileLayer.Cell cell = groundLayer.getCell(col, row);
                    if (cell != null && cell.getTile() != null) {
                        addGroundColliderForCell(groundTiles, cell, col, row, tileW, tileH);
                    }
                }
            }
            System.out.println("Ground tiles loaded: " + groundTiles.size);
        } else {
            System.out.println("⚠️ Ground layer 'ground' not found!");
        }
    }

    private void addGroundColliderForCell(Array<Rectangle> groundTiles, TiledMapTileLayer.Cell cell,
            int col, int row, float tileW, float tileH) {
        boolean addedCustomCollider = false;

        for (MapObject obj : cell.getTile().getObjects()) {
            if (obj instanceof RectangleMapObject) {
                Rectangle source = ((RectangleMapObject) obj).getRectangle();
                if (source.width <= 0f || source.height <= 0f) {
                    continue;
                }

                // Tile collision object coordinates from this map are already aligned for world
                // Y.
                float localX = source.x;
                float localY = source.y;

                float colliderW = source.width * GameConstants.GROUND_WIDTH_SCALE;
                float colliderH = source.height * GameConstants.GROUND_HEIGHT_SCALE;
                float gameX = col * tileW + localX + GameConstants.GROUND_OFFSET_X + (source.width - colliderW) * 0.5f;
                float gameY = row * tileH + localY + GameConstants.GROUND_OFFSET_Y
                        + (source.height - colliderH) * 0.5f;

                groundTiles.add(new Rectangle(gameX, gameY, colliderW, colliderH));
                addedCustomCollider = true;
            }
        }

        if (!addedCustomCollider) {
            float colliderW = tileW * GameConstants.GROUND_WIDTH_SCALE;
            float colliderH = tileH * GameConstants.GROUND_HEIGHT_SCALE;
            float gameX = col * tileW + GameConstants.GROUND_OFFSET_X + (tileW - colliderW) * 0.5f;
            float gameY = row * tileH + GameConstants.GROUND_OFFSET_Y + (tileH - colliderH) * 0.5f;
            groundTiles.add(new Rectangle(gameX, gameY, colliderW, colliderH));
        }
    }

    // This method loads the block tiles from the "block" layer of the TiledMap and
    // creates colliders for them
    private void loadBlocks(TiledMap map, Array<Rectangle> groundTiles, Array<Rectangle> blockTiles) {
        MapLayer blockLayer = map.getLayers().get("block");

        if (blockLayer == null) {
            System.out.println("⚠️ Block layer 'block' not found!");
            return;
        }

        System.out.println("Block object count: " + blockLayer.getObjects().getCount());

        for (MapObject obj : blockLayer.getObjects()) {
            if (obj instanceof RectangleMapObject) {
                Rectangle source = ((RectangleMapObject) obj).getRectangle();
                Rectangle rect = new Rectangle(source.x, source.y, source.width, source.height);

                blockTiles.add(rect);
                groundTiles.add(rect);

                System.out.println("Loaded block rect -> x:" + rect.x +
                        " y:" + rect.y +
                        " w:" + rect.width +
                        " h:" + rect.height);
            }
        }

        System.out.println("Final block collider count: " + blockTiles.size);
    }
}