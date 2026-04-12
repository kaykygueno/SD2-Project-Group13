package com.Griffith.audio;

/**
 * SOUND SYSTEM USAGE GUIDE
 * 
 * This file explains how to use the sound system in your game.
 * 
 * ============================================================
 * SETUP INSTRUCTIONS
 * ============================================================
 * 
 * 1. Create your sounds directory structure:
 * assets/sounds/
 * - player_jump.wav
 * - player_land.wav
 * - coin_collect.wav
 * - etc.
 * 
 * 2. The SoundManager is automatically initialized in Main.create()
 * No additional setup needed!
 * 
 * ============================================================
 * USAGE EXAMPLES
 * ============================================================
 * 
 * Playing a simple sound:
 * SoundManager.play(SoundType.PLAYER_JUMP);
 * 
 * Playing a sound with custom volume (0.0 to 1.0):
 * SoundManager.play(SoundType.COIN_COLLECT, 0.8f);
 * 
 * Adjusting master volume:
 * SoundManager.setMasterVolume(0.5f); // 50% volume
 * 
 * Playing looping sound (like background ambience):
 * long loopId = SoundManager.playLooping(SoundType.AMBIENCE_CAVE, 0.3f);
 * 
 * ============================================================
 * ADDING NEW SOUNDS
 * ============================================================
 * 
 * 1. Add the sound file to assets/sounds/
 * Example: assets/sounds/my_new_sound.wav
 * 
 * 2. Add an entry to the SoundType enum in SoundType.java:
 * MY_NEW_SOUND("my_new_sound.wav"),
 * 
 * 3. Use it anywhere in your code:
 * SoundManager.play(SoundType.MY_NEW_SOUND);
 * 
 * ============================================================
 * INTEGRATION EXAMPLES
 * ============================================================
 * 
 * In Coin.java (when coin is collected):
 * SoundManager.play(SoundType.COIN_COLLECT);
 * 
 * In Player.java (when jumping):
 * if (shouldJump) {
 * SoundManager.play(SoundType.PLAYER_JUMP);
 * // ... jump logic
 * }
 * 
 * In Hazard.java (when player takes damage):
 * if (collision) {
 * SoundManager.play(SoundType.HAZARD_HIT, 0.7f);
 * // ... damage logic
 * }
 * 
 * In FirstScreen.java (when level completes):
 * if (levelComplete) {
 * SoundManager.play(SoundType.LEVEL_COMPLETE);
 * }
 * 
 * ============================================================
 * RECOMMENDED SOUNDS TO CREATE
 * ============================================================
 * 
 * Essential (High Priority):
 * - player_jump.wav - Short, bright tone
 * - coin_collect.wav - Satisfying "ding" sound
 * - player_damage.wav - Alert/pain sound
 * - door_open.wav - Mechanical/magical opening
 * - level_complete.wav - Victory/celebration sound
 * 
 * Nice to Have:
 * - player_land.wav - Soft landing sound
 * - hazard_hit.wav - Different from player_damage
 * - ui_click.wav - Button/menu click
 * - ambience_cave.wav - Subtle background loop
 * 
 * ============================================================
 * SOUND ASSET RECOMMENDATIONS
 * ============================================================
 * 
 * Format: WAV or MP3 (MP3 for longer tracks like ambience)
 * Bit rate: 128 kbps is fine for game sounds
 * Sample rate: 44100 Hz
 * 
 * Free resources:
 * - Freesound.org
 * - OpenGameArt.org
 * - Zapsplat.com
 * 
 * ============================================================
 */
public class SoundUsageGuide {
    // This is just documentation - not actual code to run
}
