package com.Griffith.Tests;

import com.Griffith.main.MenuScreen;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MenuScreenTest {

    @Test
    public void nonWinMenuShowsTitleAndSubtitle() {
        MenuScreen menu = new MenuScreen(false);
        assertEquals("FIRE AND WATER", menu.getTitle());
        assertEquals("PRESS ENTER TO START", menu.getSubtitle());
    }

    @Test
    public void winMenuShowsCongratulationsAndPlayAgain() {
        MenuScreen menu = new MenuScreen(true);
        assertEquals("CONGRATULATIONS!", menu.getTitle());
        assertEquals("PRESS ENTER TO PLAY AGAIN", menu.getSubtitle());
    }
}
