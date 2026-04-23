package com.Griffith.Tests;

import com.Griffith.main.LifttUpSystem;
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

public class LifttUpSystemTest {

    // Verifies non-level-one maps do not activate the up-lift system.
    @Test
    void loadIgnoresNonLevelOneMap() {
        LifttUpSystem system = new LifttUpSystem();
        TiledMap map = createMapWithUpLift(32f, "platform-up");
        Array<Rectangle> colliders = new Array<>();

        system.load(map, "maps/levelTwo.tmx");
        system.addColliders(colliders);

        assertEquals(0, colliders.size, "no colliders should be registered outside level one");
    }

    // Verifies the up lever and platform become colliders after loading level one.
    @Test
    void loadRegistersUpLeverAndPlatformAsColliders() {
        LifttUpSystem system = new LifttUpSystem();
        TiledMap map = createMapWithUpLift(80f, "platform-up");
        Array<Rectangle> colliders = new Array<>();

        system.load(map, "maps/levelOne.tmx");
        system.addColliders(colliders);

        assertEquals(2, colliders.size, "both up platform and lever should be in active ground");
        assertTrue(containsRect(colliders, 260f, 80f, 30f, 10f), "up platform collider should be present");
        assertTrue(containsRect(colliders, 300f, 188f, 12f, 4f), "up lever collider should be present");
    }

    // Verifies reset returns the moving up platform to its initial Y position.
    @Test
    void resetRestoresPlatformToStartY() throws Exception {
        LifttUpSystem system = new LifttUpSystem();
        TiledMap map = createMapWithUpLift(96f, "platform-up");

        system.load(map, "maps/levelOne.tmx");

        Rectangle platform = getPrivateRectangle(system, "levelOnePlatformUp");
        float startY = getPrivateFloat(system, "levelOnePlatformUpStartY");
        platform.y = startY + 30f;

        system.reset();

        assertEquals(startY, platform.y, 0.0001f, "reset should place up platform back at original Y");
    }

    // Verifies level-one load splits visual cells into a dedicated runtime up-lift
    // layer.
    @Test
    void loadCreatesRuntimeVisualLayerForUpPlatform() {
        LifttUpSystem system = new LifttUpSystem();
        TiledMap map = createMapWithUpLift(80f, "platform-up");

        TiledMapTileLayer source = (TiledMapTileLayer) map.getLayers().get("lift_visual");
        source.setCell(16, 5, new TiledMapTileLayer.Cell());

        system.load(map, "maps/levelOne.tmx");

        TiledMapTileLayer runtime = (TiledMapTileLayer) map.getLayers().get("lift_visual_up_runtime");
        assertNotNull(runtime, "runtime visual layer should be created for up platform");
        assertNull(source.getCell(16, 5), "shared lift_visual layer should lose transferred up platform cell");
        assertNotNull(runtime.getCell(16, 5), "runtime up layer should receive transferred platform cell");
    }

    // Verifies the map's alternate object naming (plataform-up) is accepted.
    @Test
    void loadAcceptsPlataformUpAlias() {
        LifttUpSystem system = new LifttUpSystem();
        TiledMap map = createMapWithUpLift(72f, "plataform-up");
        Array<Rectangle> colliders = new Array<>();

        system.load(map, "maps/levelOne.tmx");
        system.addColliders(colliders);

        assertEquals(2, colliders.size, "plataform-up alias should still register up platform and lever");
        assertTrue(containsRect(colliders, 260f, 72f, 30f, 10f), "aliased up platform collider should be present");
    }

    private static TiledMap createMapWithUpLift(float platformY, String platformObjectName) {
        TiledMap map = new TiledMap();

        MapLayer interactions = new MapLayer();
        interactions.setName("interactions");
        interactions.getObjects().add(namedRect(platformObjectName, 260f, platformY, 30f, 10f));
        interactions.getObjects().add(namedRect("lever-up", 300f, 188f, 12f, 4f));
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
