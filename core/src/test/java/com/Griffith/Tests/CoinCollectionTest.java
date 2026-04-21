package com.Griffith.Tests;

import com.Griffith.Sprites.Coin;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CoinCollectionTest {

    // Verifies an overlapping player bounds collects exactly one matching-owner coin.
    @Test
    void matchingOwnerCollectsOverlappingCoin() throws Exception {
        Coin coinSystem = new Coin();
        Array<Rectangle> coins = getField(coinSystem, "coins");
        Array<Boolean> collected = getField(coinSystem, "collected");
        Array<Coin.CoinOwner> owners = getField(coinSystem, "owners");

        coins.add(rect(40f, 80f, 8f, 8f));
        collected.add(false);
        owners.add(Coin.CoinOwner.PUMPKIN);

        int collectedNow = coinSystem.checkCollection(rect(40f, 80f, 16f, 16f), Coin.CoinOwner.PUMPKIN);

        assertEquals(1, collectedNow, "overlapping matching-owner coin should be collected");
        assertTrue(collected.first(), "coin should be marked collected");
    }

    // Verifies the wrong player owner cannot collect the coin.
    @Test
    void wrongOwnerDoesNotCollectCoin() throws Exception {
        Coin coinSystem = new Coin();
        Array<Rectangle> coins = getField(coinSystem, "coins");
        Array<Boolean> collected = getField(coinSystem, "collected");
        Array<Coin.CoinOwner> owners = getField(coinSystem, "owners");

        coins.add(rect(40f, 80f, 8f, 8f));
        collected.add(false);
        owners.add(Coin.CoinOwner.DOC);

        int collectedNow = coinSystem.checkCollection(rect(40f, 80f, 16f, 16f), Coin.CoinOwner.PUMPKIN);

        assertEquals(0, collectedNow, "wrong-owner coin should not be collected");
        assertFalse(collected.first(), "coin should remain uncollected");
    }

    @SuppressWarnings("unchecked")
    private static <T> T getField(Coin coinSystem, String fieldName) throws Exception {
        Field field = Coin.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(coinSystem);
    }

    private static Rectangle rect(float x, float y, float width, float height) {
        return new Rectangle(x, y, width, height);
    }
}
