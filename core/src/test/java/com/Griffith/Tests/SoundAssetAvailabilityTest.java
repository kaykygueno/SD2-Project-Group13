package com.Griffith.Tests;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SoundAssetAvailabilityTest {

    private static final Path SOUNDS_DIR = Path.of("..", "assets", "sounds");
    private static final List<String> REQUIRED_SOUND_FILES = List.of(
            "player_run.wav",
            "player_jump.wav",
            "player_death.wav",
            "block_push.wav");

    // Verifies the gameplay sound files used by running, jumping, dying, and pushing exist in assets.
    @Test
    void requiredGameplaySoundFilesExist() {
        for (String soundFile : REQUIRED_SOUND_FILES) {
            assertTrue(Files.exists(SOUNDS_DIR.resolve(soundFile)), soundFile + " should exist in assets/sounds");
        }
    }
}
