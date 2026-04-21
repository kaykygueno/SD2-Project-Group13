package com.Griffith.Tests;

import com.Griffith.main.MovableBlockSystem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MovableBlockSystemTest {

    private static final float LEFT_BOUNDARY = 16f;
    private static final float RIGHT_BOUNDARY = 464f;

    // Verifies a player push moves the block and updates the visual offset.
    @Test
    void pushMovesBlockAndTracksVisualOffset() {
        MovableBlockSystem system = new MovableBlockSystem(LEFT_BOUNDARY, RIGHT_BOUNDARY);
        system.addBlock(rect(100f, 20f, 16f, 16f));

        MovableBlockSystem.BlockPushResult result = system.push(rect(86f, 20f, 16f, 16f), 6f);

        assertTrue(result.moved(), "block should move when the player pushes into it");
        assertEquals(106f, system.getBlocks().first().x);
        assertEquals(6f, result.getMoveX());
        assertEquals(6f, system.getVisualOffsetX());
        assertEquals(0f, system.getVisualOffsetY());
    }

    // Verifies the right-side wall stops the block before it exits the map.
    @Test
    void pushStopsAtRightBoundary() {
        MovableBlockSystem system = new MovableBlockSystem(LEFT_BOUNDARY, RIGHT_BOUNDARY);
        system.addBlock(rect(446f, 20f, 16f, 16f));

        MovableBlockSystem.BlockPushResult result = system.push(rect(432f, 20f, 16f, 16f), 8f);

        assertTrue(result.moved(), "block should still move up to the wall");
        assertEquals(2f, result.getMoveX());
        assertEquals(448f, system.getBlocks().first().x);
    }

    // Verifies a pushed block stops before overlapping the next block.
    @Test
    void pushStopsBeforeOtherBlock() {
        MovableBlockSystem system = new MovableBlockSystem(LEFT_BOUNDARY, RIGHT_BOUNDARY);
        system.addBlock(rect(100f, 20f, 16f, 16f));
        system.addBlock(rect(120f, 20f, 16f, 16f));

        MovableBlockSystem.BlockPushResult result = system.push(rect(86f, 20f, 16f, 16f), 10f);

        assertTrue(result.moved(), "leading block should move until the gap closes");
        assertEquals(4f, result.getMoveX());
        assertEquals(104f, system.getBlocks().first().x);
    }

    // Verifies reset restores the starting block position and clears the visual offset.
    @Test
    void resetReturnsBlockToStart() {
        MovableBlockSystem system = new MovableBlockSystem(LEFT_BOUNDARY, RIGHT_BOUNDARY);
        system.addBlock(rect(100f, 20f, 16f, 16f));

        system.push(rect(86f, 20f, 16f, 16f), 6f);
        system.reset();

        assertEquals(100f, system.getBlocks().first().x);
        assertEquals(20f, system.getBlocks().first().y);
        assertEquals(0f, system.getVisualOffsetX());
        assertEquals(0f, system.getVisualOffsetY());
    }

    // Verifies the landing check recognizes a downward approach onto the block top.
    @Test
    void landsOnTopRecognizesSupportedLanding() {
        MovableBlockSystem system = new MovableBlockSystem(LEFT_BOUNDARY, RIGHT_BOUNDARY);

        boolean landsOnTop = system.landsOnTop(
                rect(101f, 30f, 16f, 16f),
                -40f,
                rect(100f, 20f, 16f, 16f));

        assertTrue(landsOnTop, "player should be treated as landing on the block");
    }

    // Verifies standing detection only returns true when the actor is actually supported.
    @Test
    void standingOnTopRequiresSupport() {
        MovableBlockSystem system = new MovableBlockSystem(LEFT_BOUNDARY, RIGHT_BOUNDARY);

        assertTrue(system.isStandingOnTop(
                rect(101f, 36f, 16f, 16f),
                rect(100f, 20f, 16f, 16f)));

        assertFalse(system.isStandingOnTop(
                rect(115f, 36f, 16f, 16f),
                rect(100f, 20f, 16f, 16f)));
    }

    private static com.badlogic.gdx.math.Rectangle rect(float x, float y, float width, float height) {
        return new com.badlogic.gdx.math.Rectangle(x, y, width, height);
    }
}
