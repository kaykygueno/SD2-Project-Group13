package com.Griffith.Tests;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.zip.InflaterInputStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LevelMapIntegrityTest {

    private static final Path MAPS_DIR = Path.of("..", "assets", "maps");
    private static final List<String> LEVEL_MAPS = List.of("levelOne.tmx", "levelTwo.tmx");
    private static final List<String> REQUIRED_LAYERS = List.of(
            "backgroundtest",
            "background",
            "lift_visual",
            "surface",
            "ground",
            "coins_visual",
            "door_closed",
            "door_open");
    private static final List<String> REQUIRED_OBJECT_GROUPS = List.of(
            "coins",
            "interactions",
            "hazards",
            "spawn");

    // Verifies the level map files are present in the assets folder.
    @Test
    void levelMapsExist() {
        for (String mapName : LEVEL_MAPS) {
            assertTrue(Files.exists(MAPS_DIR.resolve(mapName)), mapName + " should exist in assets/maps");
        }
    }

    // Verifies both level maps use the expected 30x20 grid of 16px tiles.
    @Test
    void levelMapsUseExpectedTileGrid() throws Exception {
        for (String mapName : LEVEL_MAPS) {
            Document map = parseMap(mapName);
            Element root = map.getDocumentElement();

            assertEquals("30", root.getAttribute("width"), mapName + " width");
            assertEquals("20", root.getAttribute("height"), mapName + " height");
            assertEquals("16", root.getAttribute("tilewidth"), mapName + " tile width");
            assertEquals("16", root.getAttribute("tileheight"), mapName + " tile height");
        }
    }

    // Verifies each level has the required gameplay layers and object groups.
    @Test
    void levelMapsHaveRequiredLayersAndObjectGroups() throws Exception {
        for (String mapName : LEVEL_MAPS) {
            Document map = parseMap(mapName);

            for (String layerName : REQUIRED_LAYERS) {
                assertNotNull(findLayer(map, layerName), mapName + " missing layer " + layerName);
            }

            for (String objectGroupName : REQUIRED_OBJECT_GROUPS) {
                assertNotNull(findObjectGroup(map, objectGroupName), mapName + " missing objectgroup " + objectGroupName);
            }
        }
    }

    // Prevents accidental tileset references to generated build output.
    @Test
    void mapTilesetsNeverPointIntoBuildOutput() throws Exception {
        for (String mapName : LEVEL_MAPS) {
            Document map = parseMap(mapName);
            NodeList tilesets = map.getElementsByTagName("tileset");

            for (int i = 0; i < tilesets.getLength(); i++) {
                Element tileset = (Element) tilesets.item(i);
                String source = tileset.getAttribute("source");
                assertFalse(source.contains("lwjgl3/build/resources"), mapName + " has build output tileset: " + source);
                assertFalse(source.contains("../../"), mapName + " has unsafe relative tileset: " + source);
            }
        }
    }

    // Verifies every external tileset referenced by each map exists.
    @Test
    void requiredMapTilesetFilesExist() throws Exception {
        for (String mapName : LEVEL_MAPS) {
            Document map = parseMap(mapName);
            NodeList tilesets = map.getElementsByTagName("tileset");

            for (int i = 0; i < tilesets.getLength(); i++) {
                Element tileset = (Element) tilesets.item(i);
                String source = tileset.getAttribute("source");
                if (source.isBlank()) {
                    continue;
                }

                assertTrue(Files.exists(MAPS_DIR.resolve(source).normalize()),
                        mapName + " references missing tileset " + source);
            }
        }
    }

    // Verifies tile layers decode into the expected number of cells.
    @Test
    void everyRequiredTileLayerHasValidEncodedData() throws Exception {
        for (String mapName : LEVEL_MAPS) {
            Document map = parseMap(mapName);

            for (String layerName : REQUIRED_LAYERS) {
                Element layer = findLayer(map, layerName);
                int[] cells = decodeLayerCells(layer);
                assertEquals(600, cells.length, mapName + " " + layerName + " should contain 30x20 cells");
            }
        }
    }

    // Verifies important gameplay tile layers contain at least one tile.
    @Test
    void gameplayLayersAreNotEmpty() throws Exception {
        for (String mapName : LEVEL_MAPS) {
            Document map = parseMap(mapName);

            for (String layerName : List.of("ground", "lift_visual", "coins_visual", "door_closed", "door_open")) {
                Element layer = findLayer(map, layerName);
                assertTrue(countNonZeroCells(layer) > 0, mapName + " " + layerName + " should not be empty");
            }
        }
    }

    // Verifies spawn data includes both players and the finish door.
    @Test
    void spawnLayerContainsPlayersAndDoor() throws Exception {
        for (String mapName : LEVEL_MAPS) {
            Document map = parseMap(mapName);
            Element spawn = findObjectGroup(map, "spawn");

            assertNotNull(findObject(spawn, "Player1"), mapName + " missing Player1 spawn");
            assertNotNull(findObject(spawn, "Player2"), mapName + " missing Player2 spawn");
            assertNotNull(findObject(spawn, "door"), mapName + " missing door object");
        }
    }

    // Verifies lift and lever interaction objects are present.
    @Test
    void interactionsContainLiftAndButtons() throws Exception {
        for (String mapName : LEVEL_MAPS) {
            Document map = parseMap(mapName);
            Element interactions = findObjectGroup(map, "interactions");

            assertTrue(countObjectsNamed(interactions, "plataform") >= 1, mapName + " should have a lift platform");
            assertTrue(countObjectsNamed(interactions, "lever") >= 1, mapName + " should have at least one lever");
        }
    }

    // Verifies coin objects declare which player can collect them.
    @Test
    void coinObjectsHaveOwners() throws Exception {
        for (String mapName : LEVEL_MAPS) {
            Document map = parseMap(mapName);
            Element coins = findObjectGroup(map, "coins");
            NodeList objects = coins.getElementsByTagName("object");

            assertTrue(objects.getLength() > 0, mapName + " should have coin objects");
            for (int i = 0; i < objects.getLength(); i++) {
                Element object = (Element) objects.item(i);
                assertFalse(readProperty(object, "owner").isBlank(), mapName + " coin " + object.getAttribute("id") + " needs owner");
            }
        }
    }

    // Verifies level two has a visual block tile and matching collider object.
    @Test
    void levelTwoBlockVisualAndColliderAreAligned() throws Exception {
        Document map = parseMap("levelTwo.tmx");
        Element blockVisual = findLayer(map, "block_visual");
        Element blockGroup = findObjectGroup(map, "block");
        Element block = findObject(blockGroup, "white_block");

        assertNotNull(blockVisual, "levelTwo should have block_visual");
        assertNotNull(block, "levelTwo should have white_block object");
        assertEquals(1, countNonZeroCells(blockVisual), "block_visual should contain exactly one visible block tile");
        assertTrue(readFloat(block, "width") > 0f, "white block width should be positive");
        assertTrue(readFloat(block, "height") > 0f, "white block height should be positive");
    }

    // Verifies movable blocks start inside the horizontal map bounds.
    @Test
    void movableBlockStartsInsideMapBoundaries() throws Exception {
        Document map = parseMap("levelTwo.tmx");
        Element blockGroup = findObjectGroup(map, "block");
        assertNotNull(blockGroup, "levelTwo should have a block object group");
        NodeList objects = blockGroup.getElementsByTagName("object");

        for (int i = 0; i < objects.getLength(); i++) {
            Element object = (Element) objects.item(i);
            float x = readFloat(object, "x");
            float width = readFloat(object, "width");

            assertTrue(x >= 0f, "levelTwo block should start inside left edge");
            assertTrue(x + width <= 480f, "levelTwo block should start inside right edge");
        }
    }

    // Verifies both TMX files can be parsed as XML.
    @Test
    void mapsAreParseableXml() {
        for (String mapName : LEVEL_MAPS) {
            assertDoesNotThrow(() -> parseMap(mapName), mapName + " should parse as XML");
        }
    }

    private static Document parseMap(String mapName) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        Document document = factory.newDocumentBuilder().parse(MAPS_DIR.resolve(mapName).toFile());
        document.getDocumentElement().normalize();
        return document;
    }

    private static Element findLayer(Document map, String name) {
        return findNamedElement(map.getElementsByTagName("layer"), name);
    }

    private static Element findObjectGroup(Document map, String name) {
        return findNamedElement(map.getElementsByTagName("objectgroup"), name);
    }

    private static Element findNamedElement(NodeList nodes, String name) {
        for (int i = 0; i < nodes.getLength(); i++) {
            Element element = (Element) nodes.item(i);
            if (name.equals(element.getAttribute("name"))) {
                return element;
            }
        }
        return null;
    }

    private static Element findObject(Element group, String name) {
        NodeList objects = group.getElementsByTagName("object");
        return findNamedElement(objects, name);
    }

    private static int countObjectsNamed(Element group, String name) {
        NodeList objects = group.getElementsByTagName("object");
        int count = 0;
        for (int i = 0; i < objects.getLength(); i++) {
            Element object = (Element) objects.item(i);
            if (name.equals(object.getAttribute("name"))) {
                count++;
            }
        }
        return count;
    }

    private static String readProperty(Element object, String propertyName) {
        NodeList properties = object.getElementsByTagName("property");
        for (int i = 0; i < properties.getLength(); i++) {
            Element property = (Element) properties.item(i);
            if (propertyName.equals(property.getAttribute("name"))) {
                return property.getAttribute("value");
            }
        }
        return "";
    }

    private static float readFloat(Element element, String attribute) {
        return Float.parseFloat(element.getAttribute(attribute));
    }

    private static int countNonZeroCells(Element layer) throws Exception {
        int count = 0;
        for (int cell : decodeLayerCells(layer)) {
            if (cell != 0) {
                count++;
            }
        }
        return count;
    }

    private static int[] decodeLayerCells(Element layer) throws Exception {
        Element data = (Element) layer.getElementsByTagName("data").item(0);
        byte[] compressed = Base64.getDecoder().decode(data.getTextContent().trim());
        List<Byte> inflated = new ArrayList<>();

        try (InflaterInputStream input = new InflaterInputStream(new ByteArrayInputStream(compressed))) {
            int value;
            while ((value = input.read()) != -1) {
                inflated.add((byte) value);
            }
        }

        byte[] bytes = new byte[inflated.size()];
        for (int i = 0; i < inflated.size(); i++) {
            bytes[i] = inflated.get(i);
        }

        ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        int[] cells = new int[bytes.length / Integer.BYTES];
        for (int i = 0; i < cells.length; i++) {
            cells[i] = buffer.getInt();
        }
        return cells;
    }
}
