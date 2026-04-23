package com.Griffith.main;

import com.Griffith.Sprites.Button;
import com.Griffith.Sprites.Player;
import com.Griffith.gameConstants.GameConstants;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class LifttDownSystem {

    private static final float LEVEL_ONE_DOWN_LIFT_SPEED = 40f;
    private static final int LEVEL_ONE_DOWN_LIFT_BLOCKS = 3;
    private static final float LEVEL_ONE_DOWN_LIFT_ACCEL = 180f;
    private static final float LEVEL_ONE_DOWN_LIFT_MIN_SPEED = 8f;

    private Rectangle levelOneLeverDown;
    private Rectangle levelOnePlatformDown;
    private float levelOnePlatformDownStartY = 0f;
    private float levelOnePlatformLastDeltaY = 0f;
    private float levelOnePlatformVelocityY = 0f;
    private TiledMapTileLayer levelOnePlatformVisualLayer;

    // This method initializes the Level-One-only down lift pair and captures its
    // visual tiles so only that platform graphic moves.
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

            if ("lever-down".equals(obj.getName())) {
                levelOneLeverDown = copy;
            } else if ("plataform-down".equals(obj.getName())) {
                levelOnePlatformDown = copy;
                levelOnePlatformDownStartY = copy.y;
            }
        }

        if (levelOnePlatformDown == null) {
            return;
        }

        levelOnePlatformVisualLayer = buildPlatformVisualLayer(map, levelOnePlatformDown, "lift_visual_down_runtime");
        applyLevelOnePlatformVisualOffset();
    }

    public void addColliders(Array<Rectangle> activeGround) {
        if (levelOnePlatformDown != null) {
            activeGround.add(levelOnePlatformDown);
        }
        if (levelOneLeverDown != null) {
            activeGround.add(levelOneLeverDown);
        }
    }

    public void update(float delta, Player player1, Player player2, Button buttonSystem,
            Array<Rectangle> activeGround) {
        levelOnePlatformLastDeltaY = 0f;

        if (levelOneLeverDown == null || levelOnePlatformDown == null || player1 == null || player2 == null) {
            return;
        }

        boolean player1OnLever = buttonSystem.isStandingOnButton(player1.getBounds(), player1.velocityY,
                levelOneLeverDown);
        boolean player2OnLever = buttonSystem.isStandingOnButton(player2.getBounds(), player2.velocityY,
                levelOneLeverDown);
        boolean pressed = player1OnLever || player2OnLever;

        float tileHeight = levelOnePlatformVisualLayer != null ? levelOnePlatformVisualLayer.getTileHeight() : 16f;
        float maxTravel = tileHeight * LEVEL_ONE_DOWN_LIFT_BLOCKS;
        float targetY = pressed ? levelOnePlatformDownStartY - maxTravel : levelOnePlatformDownStartY;

        float deltaY = targetY - levelOnePlatformDown.y;
        float distance = Math.abs(deltaY);

        if (distance < 0.001f) {
            levelOnePlatformVelocityY = 0f;
            applyLevelOnePlatformVisualOffset();
            return;
        }

        float desiredSpeed = Math.signum(deltaY) * LEVEL_ONE_DOWN_LIFT_SPEED;
        float slowRadius = tileHeight * 1.25f;

        if (distance < slowRadius) {
            float speedScale = distance / slowRadius;
            float scaledSpeed = LEVEL_ONE_DOWN_LIFT_SPEED * speedScale;
            scaledSpeed = Math.max(LEVEL_ONE_DOWN_LIFT_MIN_SPEED, scaledSpeed);
            desiredSpeed = Math.signum(deltaY) * scaledSpeed;
        }

        levelOnePlatformVelocityY = approach(levelOnePlatformVelocityY, desiredSpeed,
                LEVEL_ONE_DOWN_LIFT_ACCEL * delta);

        float move = levelOnePlatformVelocityY * delta;
        if (Math.abs(move) > distance) {
            move = deltaY;
            levelOnePlatformVelocityY = 0f;
        }

        // Do not crush players underneath the platform when it moves downward.
        if (move < 0f) {
            float allowedDownMove = -move;
            allowedDownMove = limitDownwardMoveByPlayer(allowedDownMove, player1);
            allowedDownMove = limitDownwardMoveByPlayer(allowedDownMove, player2);
            move = -allowedDownMove;
        }

        if (move > 0f) {
            float allowedUpMove = move;
            allowedUpMove = limitUpwardMoveByPlayer(allowedUpMove, player1, activeGround);
            allowedUpMove = limitUpwardMoveByPlayer(allowedUpMove, player2, activeGround);
            move = allowedUpMove;
        }

        if (Math.abs(move) < 0.0001f) {
            applyLevelOnePlatformVisualOffset();
            return;
        }

        levelOnePlatformDown.y += move;
        levelOnePlatformLastDeltaY = move;
        applyLevelOnePlatformVisualOffset();

        carryPlayerOnLevelOneDownPlatform(player1);
        carryPlayerOnLevelOneDownPlatform(player2);
    }

    public void reset() {
        if (levelOnePlatformDown == null) {
            return;
        }

        levelOnePlatformDown.y = levelOnePlatformDownStartY;
        levelOnePlatformLastDeltaY = 0f;
        levelOnePlatformVelocityY = 0f;
        applyLevelOnePlatformVisualOffset();
    }

    private void clear() {
        levelOneLeverDown = null;
        levelOnePlatformDown = null;
        levelOnePlatformDownStartY = 0f;
        levelOnePlatformLastDeltaY = 0f;
        levelOnePlatformVelocityY = 0f;
        levelOnePlatformVisualLayer = null;
    }

    private void carryPlayerOnLevelOneDownPlatform(Player player) {
        if (player == null || player.isDead || levelOnePlatformLastDeltaY == 0f || levelOnePlatformDown == null) {
            return;
        }

        Rectangle p = player.getBounds();
        boolean standingOnTop = p.x + p.width > levelOnePlatformDown.x
                && p.x < levelOnePlatformDown.x + levelOnePlatformDown.width
                && Math.abs(p.y - (levelOnePlatformDown.y + levelOnePlatformDown.height)) < 4f
                && player.velocityY <= 0f;

        if (standingOnTop) {
            player.moveBy(0f, levelOnePlatformLastDeltaY);
        }
    }

    private void applyLevelOnePlatformVisualOffset() {
        if (levelOnePlatformVisualLayer == null || levelOnePlatformDown == null) {
            return;
        }

        float pixelDelta = levelOnePlatformDown.y - levelOnePlatformDownStartY;
        levelOnePlatformVisualLayer.setOffsetY(-pixelDelta);
    }

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

    private float limitDownwardMoveByPlayer(float allowedMove, Player player) {
        if (player == null || player.isDead || allowedMove <= 0f || levelOnePlatformDown == null) {
            return allowedMove;
        }

        Rectangle p = player.getBounds();
        float playerTop = p.y + p.height;

        boolean horizontalOverlap = p.x + p.width > levelOnePlatformDown.x
                && p.x < levelOnePlatformDown.x + levelOnePlatformDown.width;
        boolean playerUnderLift = playerTop <= levelOnePlatformDown.y + 1f;

        if (!horizontalOverlap || !playerUnderLift) {
            return allowedMove;
        }

        float maxSafeDownMove = levelOnePlatformDown.y - playerTop;
        if (maxSafeDownMove < 0f) {
            maxSafeDownMove = 0f;
        }

        return Math.min(allowedMove, maxSafeDownMove);
    }

    private float limitUpwardMoveByPlayer(float allowedMove, Player player, Array<Rectangle> activeGround) {
        if (player == null || player.isDead || allowedMove <= 0f || activeGround == null) {
            return allowedMove;
        }

        Rectangle p = player.getBounds();
        boolean standingOnTop = p.x + p.width > levelOnePlatformDown.x
                && p.x < levelOnePlatformDown.x + levelOnePlatformDown.width
                && Math.abs(p.y - (levelOnePlatformDown.y + levelOnePlatformDown.height)) < 4f
                && player.velocityY <= 0f;

        if (!standingOnTop) {
            return allowedMove;
        }

        float playerTop = p.y + p.height;
        float maxAllowed = Math.min(allowedMove, GameConstants.MAP_HEIGHT - playerTop);

        for (Rectangle tile : activeGround) {
            if (tile == null || tile == levelOnePlatformDown) {
                continue;
            }

            boolean horizontalOverlap = p.x + p.width > tile.x && p.x < tile.x + tile.width;
            if (!horizontalOverlap) {
                continue;
            }

            if (tile.y + 0.01f < playerTop) {
                continue;
            }

            float gap = tile.y - playerTop;
            if (gap < maxAllowed) {
                maxAllowed = Math.max(0f, gap);
            }
        }

        return maxAllowed;
    }

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
