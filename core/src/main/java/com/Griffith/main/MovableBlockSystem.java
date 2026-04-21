package com.Griffith.main;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class MovableBlockSystem {

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

    public BlockPushResult push(Rectangle playerBounds, float moveX) {
        if (playerBounds == null || moveX == 0f || blocks.size == 0) {
            return BlockPushResult.none();
        }

        for (Rectangle block : blocks) {
            float pushAmount = getBlockPushAmount(playerBounds, block, moveX);
            if (pushAmount == 0f) {
                continue;
            }

            float allowedMove = clampBlockMove(block, pushAmount);
            if (allowedMove != 0f) {
                block.x += allowedMove;
                updateVisualOffsets();
                return new BlockPushResult(block, allowedMove);
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
        boolean verticalOverlap = playerBounds.y + playerBounds.height > block.y + 1f
                && playerBounds.y < block.y + block.height - 1f;
        if (!verticalOverlap) {
            return 0f;
        }

        if (moveX > 0f) {
            float playerRight = playerBounds.x + playerBounds.width;
            float pushRange = Math.max(moveX + 2f, 8f);
            boolean closeToLeftSide = playerRight >= block.x - pushRange
                    && playerBounds.x < block.x + block.width * 0.5f;
            if (closeToLeftSide) {
                return moveX;
            }
        }

        if (moveX < 0f) {
            float blockRight = block.x + block.width;
            float pushRange = Math.max(-moveX + 2f, 8f);
            boolean closeToRightSide = playerBounds.x <= blockRight + pushRange
                    && playerBounds.x + playerBounds.width > block.x + block.width * 0.5f;
            if (closeToRightSide) {
                return moveX;
            }
        }

        return 0f;
    }

    private float clampBlockMove(Rectangle block, float requestedMoveX) {
        float allowedMove = requestedMoveX;

        if (requestedMoveX > 0f) {
            allowedMove = Math.min(allowedMove, rightBoundary - (block.x + block.width));
        } else {
            allowedMove = Math.max(allowedMove, leftBoundary - block.x);
        }

        Rectangle movedBlock = new Rectangle(block.x + allowedMove, block.y, block.width, block.height);

        for (Rectangle otherBlock : blocks) {
            if (otherBlock == block) {
                continue;
            }
            if (movedBlock.overlaps(otherBlock)) {
                allowedMove = limitMoveBeforeCollision(block, otherBlock, requestedMoveX);
                movedBlock.set(block.x + allowedMove, block.y, block.width, block.height);
            }
        }

        return allowedMove;
    }

    private float limitMoveBeforeCollision(Rectangle movingBlock, Rectangle obstacle, float requestedMoveX) {
        if (requestedMoveX > 0f) {
            return Math.max(0f, obstacle.x - (movingBlock.x + movingBlock.width));
        }
        return Math.min(0f, obstacle.x + obstacle.width - movingBlock.x);
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
