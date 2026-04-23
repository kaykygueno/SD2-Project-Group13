package com.Griffith.Tests;

import com.Griffith.main.LifttDownSystem;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LifttDownSystemTest {

    // Verifies non-level-one maps do not activate the lift system.
    @Test
    void loadIgnoresNonLevelOneMap() {
        LifttDownSystem system = new LifttDownSystem();
        TiledMap map = createMapWithDownLift(32f);
        Array<Rectangle> colliders = new Array<>();

        system.load(map, "maps/levelTwo.tmx");
        system.addColliders(colliders);

        assertEquals(0, colliders.size, "no colliders should be registered outside level one");
    }

    // Verifies the down lever and platform become colliders after loading level
    // one.
    @Test
    void loadRegistersDownLeverAndPlatformAsColliders() {
        LifttDownSystem system = new LifttDownSystem();
        TiledMap map = createMapWithDownLift(48f);
        Array<Rectangle> colliders = new Array<>();

        system.load(map, "maps/levelOne.tmx");
        system.addColliders(colliders);

        assertEquals(2, colliders.size, "both down platform and lever should be in active ground");
        assertTrue(containsRect(colliders, 16f, 48f, 48f, 10f), "down platform collider should be present");
        assertTrue(containsRect(colliders, 300f, 92f, 12f, 4f), "down lever collider should be present");
    }

    // Verifies reset returns the moving platform to its initial Y position.
    @Test
    void resetRestoresPlatformToStartY() throws Exception {
        LifttDownSystem system = new LifttDownSystem();
        TiledMap map = createMapWithDownLift(64f);

        system.load(map, "maps/levelOne.tmx");

        Rectangle platform = getPrivateRectangle(system, "levelOnePlatformDown");
        float startY = getPrivateFloat(system, "levelOnePlatformDownStartY");
        platform.y = startY - 25f;

        system.reset();

        assertEquals(startY, platform.y, 0.0001f, "reset should place platform back at original Y");
    }

    // Verifies level-one load splits visual cells into a dedicated runtime
    // down-lift layer.
    @Test
    void loadCreatesRuntimeVisualLayerForDownPlatform() {
        LifttDownSystem system = new LifttDownSystem();
        TiledMap map = createMapWithDownLift(48f);

        TiledMapTileLayer source = (TiledMapTileLayer) map.getLayers().get("lift_visual");
        source.setCell(1, 3, new TiledMapTileLayer.Cell());

        system.load(map, "maps/levelOne.tmx");

        TiledMapTileLayer runtime = (TiledMapTileLayer) map.getLayers().get("lift_visual_down_runtime");
        assertNotNull(runtime, "runtime visual layer should be created for down platform");
        assertNull(source.getCell(1, 3), "shared lift_visual layer should lose transferred platform cell");
        assertNotNull(runtime.getCell(1, 3), "runtime down layer should receive transferred platform cell");
    }

    private static TiledMap createMapWithDownLift(float platformY) {
        TiledMap map = new TiledMap();

        MapLayer interactions = new MapLayer();
        interactions.setName("interactions");
        interactions.getObjects().add(namedRect("plataform-down", 16f, platformY, 48f, 10f));
        interactions.getObjects().add(namedRect("lever-down", 300f, 92f, 12f, 4f));
        map.getLayers().add(interactions);

        TiledMapTileLayer liftVisual = new TiledMapTileLayer(40, 30, 16, 16);
        liftVisual.setName("lift_visual");
        map.getLayers().add(liftVisual);

        return map;
    }

    private static MapObject namedRect(String name, float x, float y, float w, float h) {
        RectangleMapObject object = new RectangleMapObject(x, y, w, h);
        object.setName(name);
        return object;
    }

    private static boolean containsRect(Array<Rectangle> rects, float x, float y, float w, float h) {
        for (Rectangle rect : rects) {
            if (Math.abs(rect.x - x) < 0.0001f
                    && Math.abs(rect.y - y) < 0.0001f
                    && Math.abs(rect.width - w) < 0.0001f
                    && Math.abs(rect.height - h) < 0.0001f) {
                return true;
            }
        }
        return false;
    }

    private static Rectangle getPrivateRectangle(Object instance, String fieldName) throws Exception {
        Field field = instance.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (Rectangle) field.get(instance);
    }

    private static float getPrivateFloat(Object instance, String fieldName) throws Exception {
        Field field = instance.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.getFloat(instance);
    }
}
