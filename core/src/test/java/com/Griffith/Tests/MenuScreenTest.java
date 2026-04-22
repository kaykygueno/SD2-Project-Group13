package com.Griffith.Tests;

import com.Griffith.main.MenuScreen;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MenuScreenTest {

    // Verifies the normal menu copy and level labels.
    @Test
    public void nonWinMenuShowsTitleAndSubtitle() {
        MenuScreen menu = new MenuScreen(false);
        assertEquals("FIRE AND WATER", menu.getTitle());
        assertEquals("SELECT A LEVEL", menu.getSubtitle());
        assertEquals("LEVEL 1 - ORIGINAL MAP", menu.getLevelOneOption());
        assertEquals("LEVEL 2 - CRYSTAL CAVERN", menu.getLevelTwoOption());
    }

    // Verifies the win menu switches to the congratulations copy.
    @Test
    public void winMenuShowsCongratulationsAndPlayAgain() {
        MenuScreen menu = new MenuScreen(true);
        assertEquals("CONGRATULATIONS!", menu.getTitle());
        assertEquals("SELECT A LEVEL TO PLAY AGAIN", menu.getSubtitle());
    }
}
