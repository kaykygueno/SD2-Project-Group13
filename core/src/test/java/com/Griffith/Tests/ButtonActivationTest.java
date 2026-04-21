package com.Griffith.Tests;

import com.Griffith.Sprites.Button;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ButtonActivationTest {

    // Verifies levers are exposed as solid colliders so players can stand on them like ground.
    @Test
    void leverActsAsSolidGround() throws Exception {
        Button buttonSystem = new Button();
        Rectangle lowerLever = rect(40f, 80f, 16f, 8f);
        Rectangle upperLever = rect(96f, 144f, 16f, 8f);
        Array<Rectangle> buttonRects = setButtonRects(buttonSystem, lowerLever, upperLever);
        Array<Rectangle> solids = new Array<>();

        buttonSystem.addButtonParts(solids);

        assertEquals(2, solids.size, "both levers should be added to the ground list");
        assertTrue(solids.contains(buttonRects.first(), true), "the first lever should behave like solid ground");
        assertTrue(solids.contains(buttonRects.peek(), true), "the second lever should behave like solid ground");
    }

    // Verifies standing on top of a lever counts as activation.
    @Test
    void standingOnTopActivatesLever() {
        Button buttonSystem = new Button();
        boolean active = buttonSystem.isStandingOnButton(rect(40f, 88f, 16f, 16f), 0f, rect(40f, 80f, 16f, 8f));

        assertTrue(active, "player standing on the lever top should activate it");
    }

    // Verifies side overlap alone does not activate the lever.
    @Test
    void touchingLeverFromSideDoesNotActivateLever() {
        Button buttonSystem = new Button();
        boolean active = buttonSystem.isStandingOnButton(rect(24f, 80f, 16f, 16f), 0f, rect(40f, 80f, 16f, 8f));

        assertFalse(active, "side contact should not activate the lever");
    }

    @SuppressWarnings("unchecked")
    private static Array<Rectangle> setButtonRects(Button buttonSystem, Rectangle... rectangles) throws Exception {
        Field field = Button.class.getDeclaredField("buttonRects");
        field.setAccessible(true);
        Array<Rectangle> buttonRects = (Array<Rectangle>) field.get(buttonSystem);
        buttonRects.clear();
        for (Rectangle rectangle : rectangles) {
            buttonRects.add(rectangle);
        }
        return buttonRects;
    }

    private static Rectangle rect(float x, float y, float width, float height) {
        return new Rectangle(x, y, width, height);
    }
}
