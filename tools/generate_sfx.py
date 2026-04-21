#!/usr/bin/env python3
"""
Generate the project's core gameplay sound effects as small procedural WAV files.

Reference targets for the generated sounds:
- player_run.wav: light stone-footstep ticks used in puzzle platformers
- player_jump.wav: short arcade jump chirp with an upward pitch glide
- player_death.wav: descending defeat sting with a soft noisy tail
- block_push.wav: muted crate/stone scrape for moving the white block

All sounds are generated in-house here so the repo does not depend on third-party
sample packs or external redistribution rules.
"""

from __future__ import annotations

import math
import random
import wave
from pathlib import Path

SAMPLE_RATE = 44_100
OUTPUT_DIR = Path(__file__).resolve().parent.parent / "assets" / "sounds"


def clamp(value: float, low: float, high: float) -> float:
    return max(low, min(high, value))


def smoothstep(x: float) -> float:
    x = clamp(x, 0.0, 1.0)
    return x * x * (3.0 - 2.0 * x)


def write_wav(path: Path, samples: list[float]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with wave.open(str(path), "wb") as wav:
        wav.setnchannels(1)
        wav.setsampwidth(2)
        wav.setframerate(SAMPLE_RATE)

        frames = bytearray()
        for sample in samples:
            value = int(clamp(sample, -1.0, 1.0) * 32767)
            frames.extend(value.to_bytes(2, "little", signed=True))
        wav.writeframes(frames)


def generate_player_run() -> list[float]:
    duration = 0.08
    samples: list[float] = []
    total = int(SAMPLE_RATE * duration)

    for index in range(total):
        t = index / SAMPLE_RATE
        progress = t / duration
        envelope = math.exp(-10.0 * progress)
        low_thump = math.sin(2.0 * math.pi * 92.0 * t)
        low_thump += 0.35 * math.sin(2.0 * math.pi * 184.0 * t)
        click = random.uniform(-1.0, 1.0) * math.exp(-30.0 * progress)
        samples.append((0.32 * low_thump + 0.16 * click) * envelope)

    return samples


def generate_player_jump() -> list[float]:
    duration = 0.14
    samples: list[float] = []
    total = int(SAMPLE_RATE * duration)

    for index in range(total):
        t = index / SAMPLE_RATE
        progress = t / duration
        frequency = 420.0 + 420.0 * smoothstep(progress)
        phase = 2.0 * math.pi * frequency * t
        envelope = math.exp(-6.0 * progress) * (1.0 - smoothstep(progress))
        body = math.sin(phase) + 0.25 * math.sin(phase * 2.0)
        sparkle = random.uniform(-1.0, 1.0) * math.exp(-18.0 * progress)
        samples.append((0.42 * body + 0.05 * sparkle) * envelope)

    return samples


def generate_player_death() -> list[float]:
    duration = 0.34
    samples: list[float] = []
    total = int(SAMPLE_RATE * duration)

    for index in range(total):
        t = index / SAMPLE_RATE
        progress = t / duration
        frequency = 360.0 - 230.0 * smoothstep(progress)
        phase = 2.0 * math.pi * max(frequency, 40.0) * t
        envelope = math.exp(-3.6 * progress)
        body = math.sin(phase) + 0.25 * math.sin(phase * 0.5)
        noise_tail = random.uniform(-1.0, 1.0) * math.exp(-6.0 * progress)
        samples.append((0.34 * body + 0.07 * noise_tail) * envelope)

    return samples


def generate_block_push() -> list[float]:
    duration = 0.22
    samples: list[float] = []
    total = int(SAMPLE_RATE * duration)

    previous_noise = 0.0
    for index in range(total):
        t = index / SAMPLE_RATE
        progress = t / duration
        envelope = math.exp(-2.8 * progress)
        raw_noise = random.uniform(-1.0, 1.0)
        previous_noise = previous_noise * 0.86 + raw_noise * 0.14
        scrape = previous_noise
        rumble = math.sin(2.0 * math.pi * 130.0 * t) * 0.4
        grit = math.sin(2.0 * math.pi * 370.0 * t) * 0.14
        samples.append((0.24 * scrape + 0.18 * rumble + grit) * envelope)

    return samples


def main() -> None:
    sounds = {
        "player_run.wav": generate_player_run(),
        "player_jump.wav": generate_player_jump(),
        "player_death.wav": generate_player_death(),
        "block_push.wav": generate_block_push(),
    }

    for name, samples in sounds.items():
        write_wav(OUTPUT_DIR / name, samples)
        print(f"Generated {OUTPUT_DIR / name}")


if __name__ == "__main__":
    main()
