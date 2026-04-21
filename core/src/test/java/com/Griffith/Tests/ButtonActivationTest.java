package com.Griffith.Tests;

import com.Griffith.Sprites.Button;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ButtonActivationTest {

    // Verifies levers are exposed as solid colliders so players can stand on them.
    @Test
    void addButtonPartsAddsLeverColliders() throws Exception {
        Button buttonSystem = new Button();
        Array<Rectangle> buttonRects = setButtonRects(buttonSystem, rect(40f, 80f, 16f, 8f));
        Array<Rectangle> solids = new Array<>();

        buttonSystem.addButtonParts(solids);

        assertSame(buttonRects.first(), solids.first(), "button colliders should be added to the ground list");
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
    void touchingLeverFromSideDoesNotActivateIt() {
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
