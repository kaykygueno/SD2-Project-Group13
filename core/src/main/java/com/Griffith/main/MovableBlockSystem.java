package com.Griffith.main;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class MovableBlockSystem {

    private static final float SIDE_CONTACT_TOLERANCE = 2f;
    private static final float VERTICAL_CONTACT_PADDING = 1f;
    private static final float MIN_MOVEMENT_EPSILON = 0.001f;

    public static final class BlockPushResult {
        private static final BlockPushResult NONE = new BlockPushResult(null, 0f);

        private final Rectangle block;
        private final float moveX;

        private BlockPushResult(Rectangle block, float moveX) {
            this.block = block;
            this.moveX = moveX;
        }

        public static BlockPushResult none() {
            return NONE;
        }

        public boolean moved() {
            return block != null && moveX != 0f;
        }

        public Rectangle getBlock() {
            return block;
        }

        public float getMoveX() {
            return moveX;
        }
    }

    private final Array<Rectangle> blocks = new Array<>();
    private final Array<Float> blockStartXs = new Array<>();
    private final Array<Float> blockStartYs = new Array<>();
    private final float leftBoundary;
    private final float rightBoundary;

    private float visualOffsetX = 0f;
    private float visualOffsetY = 0f;

    public MovableBlockSystem(float leftBoundary, float rightBoundary) {
        this.leftBoundary = leftBoundary;
        this.rightBoundary = rightBoundary;
    }

    public void clear() {
        blocks.clear();
        blockStartXs.clear();
        blockStartYs.clear();
        visualOffsetX = 0f;
        visualOffsetY = 0f;
    }

    public Rectangle addBlock(Rectangle source) {
        Rectangle block = new Rectangle(source);
        blocks.add(block);
        blockStartXs.add(block.x);
        blockStartYs.add(block.y);
        updateVisualOffsets();
        return block;
    }

    public Array<Rectangle> getBlocks() {
        return blocks;
    }

    public BlockPushResult push(Rectangle playerBounds, float moveX, Array<Rectangle> solidTiles) {
        if (playerBounds == null || moveX == 0f || blocks.size == 0) {
            return BlockPushResult.none();
        }

        for (Rectangle block : blocks) {
            float pushAmount = getBlockPushAmount(playerBounds, block, moveX);
            if (pushAmount == 0f) {
                continue;
            }

            float allowedMove = clampBlockMove(block, pushAmount, solidTiles);
            if (Math.abs(allowedMove) > MIN_MOVEMENT_EPSILON) {
                float previousX = block.x;
                block.x += allowedMove;
                clampBlockToMap(block);
                float actualMove = block.x - previousX;
                if (Math.abs(actualMove) <= MIN_MOVEMENT_EPSILON) {
                    return BlockPushResult.none();
                }

                updateVisualOffsets();
                return new BlockPushResult(block, actualMove);
            }
            return BlockPushResult.none();
        }

        return BlockPushResult.none();
    }

    public boolean landsOnTop(Rectangle actorBounds, float velocityY, Rectangle block) {
        float blockTop = block.y + block.height;
        boolean horizontallySupported = actorBounds.x + actorBounds.width > block.x + 2f
                && actorBounds.x < block.x + block.width - 2f;
        boolean landingOnTop = velocityY <= 0f
                && actorBounds.y >= blockTop - 8f
                && actorBounds.y < blockTop + 2f;

        return horizontallySupported && landingOnTop;
    }

    public boolean isStandingOnTop(Rectangle actorBounds, Rectangle block) {
        float blockTop = block.y + block.height;
        return Math.abs(actorBounds.y - blockTop) <= 2f
                && actorBounds.x + actorBounds.width > block.x + 2f
                && actorBounds.x < block.x + block.width - 2f;
    }

    public void reset() {
        for (int i = 0; i < blocks.size; i++) {
            blocks.get(i).x = blockStartXs.get(i);
            blocks.get(i).y = blockStartYs.get(i);
        }
        updateVisualOffsets();
    }

    public float getVisualOffsetX() {
        return visualOffsetX;
    }

    public float getVisualOffsetY() {
        return visualOffsetY;
    }

    private float getBlockPushAmount(Rectangle playerBounds, Rectangle block, float moveX) {
        float playerTop = playerBounds.y + playerBounds.height;
        float playerBottom = playerBounds.y;
        float blockTop = block.y + block.height;
        boolean verticalOverlap = playerTop > block.y + VERTICAL_CONTACT_PADDING
                && playerBottom < blockTop - VERTICAL_CONTACT_PADDING;
        if (!verticalOverlap) {
            return 0f;
        }

        if (moveX > 0f) {
            float previousRight = playerBounds.x + playerBounds.width - moveX;
            float playerRight = playerBounds.x + playerBounds.width;
            boolean crossedLeftFace = previousRight <= block.x + SIDE_CONTACT_TOLERANCE
                    && playerRight >= block.x - SIDE_CONTACT_TOLERANCE;
            boolean approachingFromLeft = playerBounds.x + playerBounds.width * 0.5f <= block.x + block.width * 0.5f;
            boolean closeToLeftSide = crossedLeftFace && approachingFromLeft;
            if (closeToLeftSide) {
                return moveX;
            }
        }

        if (moveX < 0f) {
            float previousLeft = playerBounds.x - moveX;
            float blockRight = block.x + block.width;
            boolean crossedRightFace = previousLeft >= blockRight - SIDE_CONTACT_TOLERANCE
                    && playerBounds.x <= blockRight + SIDE_CONTACT_TOLERANCE;
            boolean approachingFromRight = playerBounds.x + playerBounds.width * 0.5f >= block.x + block.width * 0.5f;
            boolean closeToRightSide = crossedRightFace && approachingFromRight;
            if (closeToRightSide) {
                return moveX;
            }
        }

        return 0f;
    }

    private float clampBlockMove(Rectangle block, float requestedMoveX, Array<Rectangle> solidTiles) {
        float allowedMove = requestedMoveX;

        if (requestedMoveX > 0f) {
            allowedMove = Math.min(allowedMove, rightBoundary - (block.x + block.width));
        } else {
            allowedMove = Math.max(allowedMove, leftBoundary - block.x);
        }

        allowedMove = clampAgainstBlocks(block, requestedMoveX, allowedMove);
        allowedMove = clampAgainstSolidTiles(block, requestedMoveX, allowedMove, solidTiles);

        if (Math.abs(allowedMove) <= MIN_MOVEMENT_EPSILON) {
            return 0f;
        }

        return allowedMove;
    }

    private float clampAgainstBlocks(Rectangle block, float requestedMoveX, float allowedMove) {
        Rectangle movedBlock = new Rectangle(block.x + allowedMove, block.y, block.width, block.height);

        for (Rectangle otherBlock : blocks) {
            if (otherBlock == block || !movedBlock.overlaps(otherBlock)) {
                continue;
            }

            float limitedMove = limitMoveBeforeCollision(block, otherBlock, requestedMoveX);
            allowedMove = requestedMoveX > 0f
                    ? Math.min(allowedMove, limitedMove)
                    : Math.max(allowedMove, limitedMove);
            movedBlock.set(block.x + allowedMove, block.y, block.width, block.height);
        }

        return allowedMove;
    }

    private float clampAgainstSolidTiles(Rectangle block, float requestedMoveX, float allowedMove, Array<Rectangle> solidTiles) {
        if (solidTiles == null || solidTiles.size == 0) {
            return allowedMove;
        }

        Rectangle movedBlock = new Rectangle(block.x + allowedMove, block.y, block.width, block.height);

        for (Rectangle solidTile : solidTiles) {
            if (!movedBlock.overlaps(solidTile)) {
                continue;
            }

            float limitedMove = limitMoveBeforeCollision(block, solidTile, requestedMoveX);
            allowedMove = requestedMoveX > 0f
                    ? Math.min(allowedMove, limitedMove)
                    : Math.max(allowedMove, limitedMove);
            movedBlock.set(block.x + allowedMove, block.y, block.width, block.height);
        }

        return allowedMove;
    }

    private float limitMoveBeforeCollision(Rectangle movingBlock, Rectangle obstacle, float requestedMoveX) {
        if (requestedMoveX > 0f) {
            return Math.max(0f, obstacle.x - (movingBlock.x + movingBlock.width));
        }
        return Math.min(0f, obstacle.x + obstacle.width - movingBlock.x);
    }

    private void clampBlockToMap(Rectangle block) {
        float minX = leftBoundary;
        float maxX = rightBoundary - block.width;
        if (block.x < minX) {
            block.x = minX;
        } else if (block.x > maxX) {
            block.x = maxX;
        }
    }

    private void updateVisualOffsets() {
        if (blocks.size == 0 || blockStartXs.size == 0 || blockStartYs.size == 0) {
            visualOffsetX = 0f;
            visualOffsetY = 0f;
            return;
        }

        Rectangle firstBlock = blocks.first();
        visualOffsetX = firstBlock.x - blockStartXs.first();
        visualOffsetY = firstBlock.y - blockStartYs.first();
    }
}
