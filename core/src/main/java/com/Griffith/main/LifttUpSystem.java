package com.Griffith.main;

import com.Griffith.Sprites.Button;
import com.Griffith.Sprites.Player;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class LifttUpSystem {

    private static final float LEVEL_ONE_UP_LIFT_SPEED = 40f;
    private static final int LEVEL_ONE_UP_LIFT_BLOCKS = 8;
    private static final float LEVEL_ONE_UP_LIFT_ACCEL = 180f;
    private static final float LEVEL_ONE_UP_LIFT_MIN_SPEED = 8f;

    private Rectangle levelOneLeverUp;
    private Rectangle levelOnePlatformUp;
    private float levelOnePlatformUpStartY = 0f;
    private float levelOnePlatformLastDeltaY = 0f;
    private float levelOnePlatformVelocityY = 0f;
    private TiledMapTileLayer levelOnePlatformVisualLayer;

    // This method initializes the Level-One-only up lift pair and creates a
    // dedicated runtime visual layer for that platform.
    public void load(TiledMap map, String mapPath) {
        clear();

        if (!"maps/levelOne.tmx".equals(mapPath)) {
            return;
        }

        MapLayer interactions = map.getLayers().get("interactions");
        if (interactions == null) {
            return;
        }

        for (MapObject obj : interactions.getObjects()) {
            if (!(obj instanceof RectangleMapObject)) {
                continue;
            }

            Rectangle source = ((RectangleMapObject) obj).getRectangle();
            Rectangle copy = new Rectangle(source.x, source.y, source.width, source.height);

            if ("lever-up".equals(obj.getName())) {
                levelOneLeverUp = copy;
            } else if ("platform-up".equals(obj.getName()) || "plataform-up".equals(obj.getName())) {
                levelOnePlatformUp = copy;
                levelOnePlatformUpStartY = copy.y;
            }
        }

        levelOnePlatformVisualLayer = buildPlatformVisualLayer(map, levelOnePlatformUp, "lift_visual_up_runtime");
        applyLevelOnePlatformVisualOffset();
    }

    public void addColliders(Array<Rectangle> activeGround) {
        if (levelOnePlatformUp != null) {
            activeGround.add(levelOnePlatformUp);
        }
        if (levelOneLeverUp != null) {
            activeGround.add(levelOneLeverUp);
        }
    }

    // This method raises the platform while a player stands on the matching lever
    // and restores it when the lever is released.
    public void update(float delta, Player player1, Player player2, Button buttonSystem) {
        levelOnePlatformLastDeltaY = 0f;

        if (levelOneLeverUp == null || levelOnePlatformUp == null || player1 == null || player2 == null) {
            return;
        }

        boolean player1OnLever = buttonSystem.isStandingOnButton(player1.getBounds(), player1.velocityY,
                levelOneLeverUp);
        boolean player2OnLever = buttonSystem.isStandingOnButton(player2.getBounds(), player2.velocityY,
                levelOneLeverUp);
        boolean pressed = player1OnLever || player2OnLever;

        float tileHeight = levelOnePlatformVisualLayer != null ? levelOnePlatformVisualLayer.getTileHeight() : 16f;
        float maxTravel = tileHeight * LEVEL_ONE_UP_LIFT_BLOCKS;
        float targetY = pressed ? levelOnePlatformUpStartY + maxTravel : levelOnePlatformUpStartY;

        float deltaY = targetY - levelOnePlatformUp.y;
        float distance = Math.abs(deltaY);

        if (distance < 0.001f) {
            levelOnePlatformVelocityY = 0f;
            applyLevelOnePlatformVisualOffset();
            return;
        }

        float desiredSpeed = Math.signum(deltaY) * LEVEL_ONE_UP_LIFT_SPEED;
        float slowRadius = tileHeight * 1.25f;

        if (distance < slowRadius) {
            float speedScale = distance / slowRadius;
            float scaledSpeed = LEVEL_ONE_UP_LIFT_SPEED * speedScale;
            scaledSpeed = Math.max(LEVEL_ONE_UP_LIFT_MIN_SPEED, scaledSpeed);
            desiredSpeed = Math.signum(deltaY) * scaledSpeed;
        }

        levelOnePlatformVelocityY = approach(levelOnePlatformVelocityY, desiredSpeed, LEVEL_ONE_UP_LIFT_ACCEL * delta);

        float move = levelOnePlatformVelocityY * delta;
        if (Math.abs(move) > distance) {
            move = deltaY;
            levelOnePlatformVelocityY = 0f;
        }

        if (Math.abs(move) < 0.0001f) {
            applyLevelOnePlatformVisualOffset();
            return;
        }

        levelOnePlatformUp.y += move;
        levelOnePlatformLastDeltaY = move;
        applyLevelOnePlatformVisualOffset();

        carryPlayerOnLevelOneUpPlatform(player1);
        carryPlayerOnLevelOneUpPlatform(player2);
    }

    public void reset() {
        if (levelOnePlatformUp == null) {
            return;
        }

        levelOnePlatformUp.y = levelOnePlatformUpStartY;
        levelOnePlatformLastDeltaY = 0f;
        levelOnePlatformVelocityY = 0f;
        applyLevelOnePlatformVisualOffset();
    }

    // This method clears the cached level-one lift state before loading a map.
    private void clear() {
        levelOneLeverUp = null;
        levelOnePlatformUp = null;
        levelOnePlatformUpStartY = 0f;
        levelOnePlatformLastDeltaY = 0f;
        levelOnePlatformVelocityY = 0f;
        levelOnePlatformVisualLayer = null;
    }

    // This method carries any player standing on the platform so the player stays
    // attached to the moving surface.
    private void carryPlayerOnLevelOneUpPlatform(Player player) {
        if (player == null || player.isDead || levelOnePlatformLastDeltaY == 0f || levelOnePlatformUp == null) {
            return;
        }

        Rectangle p = player.getBounds();
        boolean standingOnTop = p.x + p.width > levelOnePlatformUp.x
                && p.x < levelOnePlatformUp.x + levelOnePlatformUp.width
                && Math.abs(p.y - (levelOnePlatformUp.y + levelOnePlatformUp.height)) < 4f
                && player.velocityY <= 0f;

        if (standingOnTop) {
            player.moveBy(0f, levelOnePlatformLastDeltaY);
        }
    }

    // This method keeps the runtime visual layer aligned with the platform collider.
    private void applyLevelOnePlatformVisualOffset() {
        if (levelOnePlatformVisualLayer == null || levelOnePlatformUp == null) {
            return;
        }

        float pixelDelta = levelOnePlatformUp.y - levelOnePlatformUpStartY;
        levelOnePlatformVisualLayer.setOffsetY(-pixelDelta);
    }

    // This method clones only the platform tiles from the shared lift layer into a
    // dedicated runtime layer so each platform can move independently.
    private TiledMapTileLayer buildPlatformVisualLayer(TiledMap map, Rectangle platformRect, String layerName) {
        MapLayer sharedVisualLayer = map.getLayers().get("lift_visual");
        if (!(sharedVisualLayer instanceof TiledMapTileLayer) || platformRect == null) {
            return null;
        }

        TiledMapTileLayer sourceLayer = (TiledMapTileLayer) sharedVisualLayer;
        TiledMapTileLayer platformLayer = new TiledMapTileLayer(
                sourceLayer.getWidth(),
                sourceLayer.getHeight(),
                sourceLayer.getTileWidth(),
                sourceLayer.getTileHeight());
        platformLayer.setName(layerName);

        float tileWidth = sourceLayer.getTileWidth();
        float tileHeight = sourceLayer.getTileHeight();

        int startCol = Math.max(0, (int) Math.floor(platformRect.x / tileWidth));
        int endCol = Math.min(sourceLayer.getWidth() - 1,
                (int) Math.floor((platformRect.x + platformRect.width - 0.001f) / tileWidth));
        int startRow = Math.max(0, (int) Math.floor(platformRect.y / tileHeight) - 1);
        int endRow = Math.min(sourceLayer.getHeight() - 1,
                (int) Math.floor((platformRect.y + platformRect.height - 0.001f) / tileHeight) + 1);

        boolean copiedAnyCell = false;

        for (int row = startRow; row <= endRow; row++) {
            for (int col = startCol; col <= endCol; col++) {
                TiledMapTileLayer.Cell cell = sourceLayer.getCell(col, row);
                if (cell == null) {
                    continue;
                }

                Rectangle cellRect = new Rectangle(col * tileWidth, row * tileHeight, tileWidth, tileHeight);
                if (!cellRect.overlaps(platformRect)) {
                    continue;
                }

                platformLayer.setCell(col, row, cell);
                sourceLayer.setCell(col, row, null);
                copiedAnyCell = true;
            }
        }

        if (!copiedAnyCell) {
            return null;
        }

        map.getLayers().add(platformLayer);
        return platformLayer;
    }

    // This method smoothly moves the current velocity toward the requested target
    // speed without overshooting.
    private float approach(float current, float target, float maxDelta) {
        if (current < target) {
            return Math.min(current + maxDelta, target);
        }
        if (current > target) {
            return Math.max(current - maxDelta, target);
        }
        return current;
    }
}
