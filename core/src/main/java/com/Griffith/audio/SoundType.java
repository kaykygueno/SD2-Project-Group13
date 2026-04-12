package com.Griffith.audio;

/**
 * Enum of all sound types in the game.
 * Add new sounds here and they'll be automatically managed by SoundManager.
 * 
 * Each enum value maps to a file in the sounds/ directory.
 * File naming convention: lowercase with underscores, ending in .mp3 or .wav
 */
public enum SoundType {

    // Player sounds
    PLAYER_JUMP("player_jump.wav"),
    PLAYER_LAND("player_land.wav"),
    PLAYER_DAMAGE("player_damage.wav"),
    PLAYER_DEATH("player_death.wav"),

    // Collectibles
    COIN_COLLECT("coin_collect.wav"),

    // Hazards
    HAZARD_HIT("hazard_hit.wav"),
    LAVA_DAMAGE("lava_damage.wav"),

    // Doors & Level progression
    DOOR_OPEN("door_open.wav"),
    DOOR_CLOSE("door_close.wav"),
    LEVEL_COMPLETE("level_complete.wav"),

    // UI
    UI_CLICK("ui_click.wav"),
    UI_SELECT("ui_select.wav"),

    // Ambience (looping)
    AMBIENCE_CAVE("ambience_cave.wav");

    private final String fileName;

    SoundType(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Get the filename for this sound type.
     */
    public String getFileName() {
        return fileName;
    }
}
