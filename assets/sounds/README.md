# Sound Assets Directory

The core movement sounds in this project are generated in-house by
`tools/generate_sfx.py`.

Reference targets documented as comments in that script:
- `player_run.wav`: light stone-footstep ticks
- `player_jump.wav`: short arcade jump chirp
- `player_death.wav`: descending defeat sting
- `block_push.wav`: muted crate/stone scrape

Create sound files in this directory with the naming convention used in `SoundType.java`.

## Directory Structure
```
assets/
├── sounds/
│   ├── player_jump.wav
│   ├── player_run.wav
│   ├── player_land.wav
│   ├── coin_collect.wav
│   ├── player_damage.wav
│   ├── player_death.wav
│   ├── block_push.wav
│   ├── hazard_hit.wav
│   ├── lava_damage.wav
│   ├── door_open.wav
│   ├── door_close.wav
│   ├── level_complete.wav
│   ├── ui_click.wav
│   ├── ui_select.wav
│   └── ambience_cave.wav
├── maps/
└── images/
```

## Next Steps
1. Decide on your audio assets
2. Place .wav or .mp3 files in this directory with the correct filenames
3. Sounds will automatically be loaded when you call `SoundManager.play(SoundType.COIN_COLLECT)` etc.

## Sound File Naming
- All lowercase
- Use underscores for spaces (e.g., `player_jump.wav` not `player jump.wav`)
- Match the filename in the `SoundType` enum
