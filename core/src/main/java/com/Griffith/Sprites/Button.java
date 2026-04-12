package com.Griffith.Sprites;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class Button {

    private final Array<Rectangle> buttonRects = new Array<>();
    private final Array<Rectangle> liftParts = new Array<>();
    private final Array<Float> liftStartYs = new Array<>();

    private MapLayer liftVisualLayer;
    private boolean liftActive = false;
    private float lastLiftDeltaY = 0f;
    private float liftVisualOffsetY = 0f;

    private static final float LIFT_SPEED = 40f;
    private static final float LIFT_TRAVEL_DISTANCE = 70f;

    // This method loads the button and lift rectangles from the interactions layer
    // of the map.
    public void loadInteractions(TiledMap map) {
        MapLayer interactionLayer = map.getLayers().get("interactions");
        if (interactionLayer == null) {
            System.out.println("⚠️ interactions layer not found!");
            return;
        }

        buttonRects.clear();
        liftParts.clear();
        liftStartYs.clear();

        for (MapObject obj : interactionLayer.getObjects()) {
            if (obj instanceof RectangleMapObject) {
                Rectangle source = ((RectangleMapObject) obj).getRectangle();
                Rectangle rect = new Rectangle(source.x, source.y, source.width, source.height);

                if ("lever".equals(obj.getName())) {
                    buttonRects.add(rect);
                } else if ("plataform".equals(obj.getName())) {
                    liftParts.add(rect);
                    liftStartYs.add(rect.y);
                }
            }
        }

        System.out.println("Levers loaded: " + buttonRects.size);
        System.out.println("Lift parts loaded: " + liftParts.size);
    }

    // This method locates the lift visual layer so the moving platform graphics can
    // be offset with the collider.
    public void loadLiftVisualLayer(TiledMap map) {
        liftVisualLayer = map.getLayers().get("lift_visual");

        if (liftVisualLayer == null) {
            System.out.println("⚠️ lift_visual layer not found!");
        } else {
            liftVisualLayer.setOffsetY(0f);
            System.out.println("Lift visual layer loaded.");
        }
    }

    // This method appends the current lift colliders to the provided ground list
    // for player collision checks.
    public void addLiftParts(Array<Rectangle> target) {
        target.addAll(liftParts);
    }

    // This method updates lift movement and carries any player standing on top of
    // the lift.
    public void update(float delta, Player player1, Player player2) {
        updateLift(delta, player1, player2);

        if (player1 != null) {
            applyLiftCarry(player1);
        }
        if (player2 != null) {
            applyLiftCarry(player2);
        }
    }

    // This method exposes the button rectangles for debug drawing and external
    // checks.
    public Array<Rectangle> getButtonRects() {
        return buttonRects;
    }

    // This method exposes the lift colliders for debug drawing and ground
    // composition.
    public Array<Rectangle> getLiftParts() {
        return liftParts;
    }

    // This method restores the lift to its original position and clears its
    // movement state.
    public void reset() {
        for (int i = 0; i < liftParts.size; i++) {
            liftParts.get(i).y = liftStartYs.get(i);
        }

        liftVisualOffsetY = 0f;
        applyLiftVisualOffset();

        liftActive = false;
        lastLiftDeltaY = 0f;
    }

    // This method moves the lift up or down depending on whether any player is
    // pressing a button.
    private void updateLift(float delta, Player player1, Player player2) {
        lastLiftDeltaY = 0f;

        if (buttonRects.size == 0 || liftParts.size == 0) {
            return;
        }

        boolean anyPlayerOnButton = false;

        for (Rectangle button : buttonRects) {
            boolean player1OnButton = player1 != null && player1.getBounds().overlaps(button);
            boolean player2OnButton = player2 != null && player2.getBounds().overlaps(button);

            if (player1OnButton || player2OnButton) {
                anyPlayerOnButton = true;
                break;
            }
        }

        liftActive = anyPlayerOnButton;

        float moveAmount = LIFT_SPEED * delta;

        if (liftActive) {
            float allowedMove = moveAmount;

            for (int i = 0; i < liftParts.size; i++) {
                float currentY = liftParts.get(i).y;
                float maxY = liftStartYs.get(i) + LIFT_TRAVEL_DISTANCE;
                float remaining = maxY - currentY;
                if (remaining < allowedMove) {
                    allowedMove = remaining;
                }
            }

            if (allowedMove > 0f) {
                for (Rectangle part : liftParts) {
                    part.y += allowedMove;
                }
                liftVisualOffsetY += allowedMove;
                applyLiftVisualOffset();
                lastLiftDeltaY = allowedMove;
            }
        } else {
            float allowedMove = moveAmount;

            for (int i = 0; i < liftParts.size; i++) {
                float currentY = liftParts.get(i).y;
                float minY = liftStartYs.get(i);
                float remaining = currentY - minY;
                if (remaining < allowedMove) {
                    allowedMove = remaining;
                }
            }

            // Do not crush players underneath the lift; stop at the player's top.
            allowedMove = limitDownwardMoveByPlayer(allowedMove, player1);
            allowedMove = limitDownwardMoveByPlayer(allowedMove, player2);

            if (allowedMove > 0f) {
                for (Rectangle part : liftParts) {
                    part.y -= allowedMove;
                }
                liftVisualOffsetY -= allowedMove;
                applyLiftVisualOffset();
                lastLiftDeltaY = -allowedMove;
            }
        }
    }

    // This method limits lift descent so it never moves below a player's head when
    // the player is underneath it.
    private float limitDownwardMoveByPlayer(float allowedMove, Player player) {
        if (player == null || allowedMove <= 0f) {
            return allowedMove;
        }

        Rectangle p = player.getBounds();
        float playerTop = p.y + p.height;

        for (Rectangle part : liftParts) {
            boolean horizontalOverlap = p.x + p.width > part.x && p.x < part.x + part.width;
            boolean playerUnderLift = playerTop <= part.y + 1f;

            if (!horizontalOverlap || !playerUnderLift) {
                continue;
            }

            float maxSafeDownMove = part.y - playerTop;
            if (maxSafeDownMove < 0f) {
                maxSafeDownMove = 0f;
            }

            if (maxSafeDownMove < allowedMove) {
                allowedMove = maxSafeDownMove;
            }
        }

        return allowedMove;
    }

    // This method applies the accumulated visual offset to the lift tile layer.
    private void applyLiftVisualOffset() {
        if (liftVisualLayer != null) {
            liftVisualLayer.setOffsetY(-liftVisualOffsetY);
        }
    }

    // This method moves a player together with the lift when the player is standing
    // on top of it.
    private void applyLiftCarry(Player player) {
        if (player == null || lastLiftDeltaY == 0f || liftParts.size == 0) {
            return;
        }

        Rectangle p = player.getBounds();

        for (Rectangle part : liftParts) {
            boolean standingOnTop = p.x + p.width > part.x &&
                    p.x < part.x + part.width &&
                    Math.abs(p.y - (part.y + part.height)) < 4f &&
                    player.velocityY <= 0;

            if (standingOnTop) {
                player.moveBy(0f, lastLiftDeltaY);
                break;
            }
        }
    }
}
