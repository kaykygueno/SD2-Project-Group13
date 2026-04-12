package com.Griffith.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import java.util.HashMap;
import java.util.Map;

/**
 * Centralized sound management system for the game.
 * Handles loading, caching, and playing all sound effects.
 * 
 * Usage:
 * - Initialize once in Main.create(): SoundManager.init()
 * - Play sounds anywhere: SoundManager.play(SoundType.COIN_COLLECT)
 * - Dispose in Main.dispose(): SoundManager.dispose()
 */
public class SoundManager {

    private static final Map<SoundType, Sound> soundCache = new HashMap<>();
    private static boolean initialized = false;
    private static float masterVolume = 1.0f;

    // Base path for all sound assets
    private static final String SOUNDS_PATH = "sounds/";

    /**
     * Initialize the sound manager.
     * Call this once in Main.create()
     */
    public static void init() {
        if (initialized)
            return;
        initialized = true;
        // Sounds are loaded on-demand to save memory
    }

    /**
     * Play a sound effect.
     * 
     * @param soundType The type of sound to play
     */
    public static void play(SoundType soundType) {
        play(soundType, 1.0f);
    }

    /**
     * Play a sound effect with custom volume.
     * 
     * @param soundType The type of sound to play
     * @param volume    Volume multiplier (0.0 to 1.0+)
     */
    public static void play(SoundType soundType, float volume) {
        if (!initialized)
            return;

        try {
            Sound sound = getOrLoadSound(soundType);
            if (sound != null) {
                sound.play(volume * masterVolume);
            }
        } catch (Exception e) {
            Gdx.app.log("SoundManager", "Error playing sound: " + soundType + " - " + e.getMessage());
        }
    }

    /**
     * Play a looping sound effect.
     * 
     * @param soundType The type of sound to play
     * @return The sound ID (needed to stop it later)
     */
    public static long playLooping(SoundType soundType) {
        return playLooping(soundType, 1.0f);
    }

    /**
     * Play a looping sound effect with custom volume.
     * 
     * @param soundType The type of sound to play
     * @param volume    Volume multiplier (0.0 to 1.0+)
     * @return The sound ID (needed to stop it later)
     */
    public static long playLooping(SoundType soundType, float volume) {
        if (!initialized)
            return -1;

        try {
            Sound sound = getOrLoadSound(soundType);
            if (sound != null) {
                return sound.loop(volume * masterVolume);
            }
        } catch (Exception e) {
            Gdx.app.log("SoundManager", "Error playing looping sound: " + soundType + " - " + e.getMessage());
        }
        return -1;
    }

    /**
     * Stop a sound that was started with playLooping.
     * 
     * @param soundId The ID returned from playLooping
     */
    public static void stop(long soundId) {
        if (soundId >= 0) {
            try {
                // Find and stop the sound - note: LibGDX doesn't have a direct stop by ID for
                // Sound interface
                // You would need to store the sound reference if you need to stop it
                // For now, this is a placeholder
            } catch (Exception e) {
                Gdx.app.log("SoundManager", "Error stopping sound: " + e.getMessage());
            }
        }
    }

    /**
     * Set the master volume for all sounds.
     * 
     * @param volume Volume multiplier (0.0 to 1.0+)
     */
    public static void setMasterVolume(float volume) {
        masterVolume = Math.max(0.0f, volume);
    }

    /**
     * Get the current master volume.
     */
    public static float getMasterVolume() {
        return masterVolume;
    }

    /**
     * Get or load a sound from cache, loading it if necessary.
     */
    private static Sound getOrLoadSound(SoundType soundType) {
        if (!soundCache.containsKey(soundType)) {
            String path = SOUNDS_PATH + soundType.getFileName();
            try {
                Sound sound = Gdx.audio.newSound(Gdx.files.internal(path));
                soundCache.put(soundType, sound);
            } catch (Exception e) {
                Gdx.app.log("SoundManager", "Failed to load sound: " + path);
                return null;
            }
        }
        return soundCache.get(soundType);
    }

    /**
     * Dispose all cached sounds. Call this in Main.dispose()
     */
    public static void dispose() {
        for (Sound sound : soundCache.values()) {
            if (sound != null) {
                sound.dispose();
            }
        }
        soundCache.clear();
        initialized = false;
    }
}
