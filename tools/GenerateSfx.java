import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

/**
 * Generate the project's core gameplay sound effects as small procedural WAV files.
 *
 * Reference targets for the generated sounds:
 * - player_run.wav: light stone-footstep ticks used in puzzle platformers
 * - player_jump.wav: short arcade jump chirp with an upward pitch glide
 * - player_death.wav: descending defeat sting with a soft noisy tail
 * - block_push.wav: muted crate/stone scrape for moving the white block
 * - coin_collect.wav: bright pickup chime with a tiny sparkle tail
 *
 * All sounds are generated in-house here so the repo does not depend on third-party
 * sample packs or external redistribution rules.
 */
public final class GenerateSfx {

    private static final int SAMPLE_RATE = 44_100;
    private static final Path OUTPUT_DIR = Path.of("assets", "sounds");
    private static final Random RANDOM = new Random(13_371L);

    private GenerateSfx() {
    }

    public static void main(String[] args) throws IOException {
        Map<String, float[]> sounds = new LinkedHashMap<>();
        sounds.put("player_run.wav", generatePlayerRun());
        sounds.put("player_jump.wav", generatePlayerJump());
        sounds.put("player_death.wav", generatePlayerDeath());
        sounds.put("block_push.wav", generateBlockPush());
        sounds.put("coin_collect.wav", generateCoinCollect());

        for (Map.Entry<String, float[]> entry : sounds.entrySet()) {
            Path path = OUTPUT_DIR.resolve(entry.getKey());
            writeWav(path, entry.getValue());
            System.out.println("Generated " + path.toAbsolutePath());
        }
    }

    private static float[] generatePlayerRun() {
        double duration = 0.08;
        int total = (int) (SAMPLE_RATE * duration);
        float[] samples = new float[total];

        for (int index = 0; index < total; index++) {
            double t = index / (double) SAMPLE_RATE;
            double progress = t / duration;
            double envelope = Math.exp(-10.0 * progress);
            double lowThump = Math.sin(2.0 * Math.PI * 92.0 * t);
            lowThump += 0.35 * Math.sin(2.0 * Math.PI * 184.0 * t);
            double click = randomSigned() * Math.exp(-30.0 * progress);
            samples[index] = (float) ((0.32 * lowThump + 0.16 * click) * envelope);
        }

        return samples;
    }

    private static float[] generatePlayerJump() {
        double duration = 0.14;
        int total = (int) (SAMPLE_RATE * duration);
        float[] samples = new float[total];

        for (int index = 0; index < total; index++) {
            double t = index / (double) SAMPLE_RATE;
            double progress = t / duration;
            double frequency = 420.0 + 420.0 * smoothstep(progress);
            double phase = 2.0 * Math.PI * frequency * t;
            double envelope = Math.exp(-6.0 * progress) * (1.0 - smoothstep(progress));
            double body = Math.sin(phase) + 0.25 * Math.sin(phase * 2.0);
            double sparkle = randomSigned() * Math.exp(-18.0 * progress);
            samples[index] = (float) ((0.42 * body + 0.05 * sparkle) * envelope);
        }

        return samples;
    }

    private static float[] generatePlayerDeath() {
        double duration = 0.34;
        int total = (int) (SAMPLE_RATE * duration);
        float[] samples = new float[total];

        for (int index = 0; index < total; index++) {
            double t = index / (double) SAMPLE_RATE;
            double progress = t / duration;
            double frequency = 360.0 - 230.0 * smoothstep(progress);
            double phase = 2.0 * Math.PI * Math.max(frequency, 40.0) * t;
            double envelope = Math.exp(-3.6 * progress);
            double body = Math.sin(phase) + 0.25 * Math.sin(phase * 0.5);
            double noiseTail = randomSigned() * Math.exp(-6.0 * progress);
            samples[index] = (float) ((0.34 * body + 0.07 * noiseTail) * envelope);
        }

        return samples;
    }

    private static float[] generateBlockPush() {
        double duration = 0.22;
        int total = (int) (SAMPLE_RATE * duration);
        float[] samples = new float[total];

        double previousNoise = 0.0;
        for (int index = 0; index < total; index++) {
            double t = index / (double) SAMPLE_RATE;
            double progress = t / duration;
            double envelope = Math.exp(-2.8 * progress);
            double rawNoise = randomSigned();
            previousNoise = previousNoise * 0.86 + rawNoise * 0.14;
            double scrape = previousNoise;
            double rumble = Math.sin(2.0 * Math.PI * 130.0 * t) * 0.4;
            double grit = Math.sin(2.0 * Math.PI * 370.0 * t) * 0.14;
            samples[index] = (float) ((0.24 * scrape + 0.18 * rumble + grit) * envelope);
        }

        return samples;
    }

    private static float[] generateCoinCollect() {
        double duration = 0.18;
        int total = (int) (SAMPLE_RATE * duration);
        float[] samples = new float[total];

        for (int index = 0; index < total; index++) {
            double t = index / (double) SAMPLE_RATE;
            double progress = t / duration;
            double primaryFrequency = 920.0 + 160.0 * smoothstep(progress);
            double overtoneFrequency = primaryFrequency * 1.5;
            double envelope = Math.exp(-7.5 * progress);
            double body = Math.sin(2.0 * Math.PI * primaryFrequency * t);
            body += 0.45 * Math.sin(2.0 * Math.PI * overtoneFrequency * t);
            double sparkle = randomSigned() * Math.exp(-22.0 * progress);
            samples[index] = (float) ((0.28 * body + 0.04 * sparkle) * envelope);
        }

        return samples;
    }

    private static void writeWav(Path path, float[] samples) throws IOException {
        Files.createDirectories(path.getParent());

        try (ByteArrayOutputStream frameBytes = new ByteArrayOutputStream();
                DataOutputStream frameData = new DataOutputStream(frameBytes)) {
            for (float sample : samples) {
                short value = (short) Math.round(clamp(sample, -1.0, 1.0) * 32_767.0);
                frameData.writeShort(Short.reverseBytes(value));
            }
            frameData.flush();

            byte[] pcm = frameBytes.toByteArray();
            int dataSize = pcm.length;
            int riffChunkSize = 36 + dataSize;
            int byteRate = SAMPLE_RATE * 2;

            try (DataOutputStream out = new DataOutputStream(Files.newOutputStream(path))) {
                out.writeBytes("RIFF");
                writeIntLE(out, riffChunkSize);
                out.writeBytes("WAVE");
                out.writeBytes("fmt ");
                writeIntLE(out, 16);
                writeShortLE(out, (short) 1);
                writeShortLE(out, (short) 1);
                writeIntLE(out, SAMPLE_RATE);
                writeIntLE(out, byteRate);
                writeShortLE(out, (short) 2);
                writeShortLE(out, (short) 16);
                out.writeBytes("data");
                writeIntLE(out, dataSize);
                out.write(pcm);
            }
        }
    }

    private static void writeIntLE(DataOutputStream out, int value) throws IOException {
        out.writeByte(value & 0xFF);
        out.writeByte((value >>> 8) & 0xFF);
        out.writeByte((value >>> 16) & 0xFF);
        out.writeByte((value >>> 24) & 0xFF);
    }

    private static void writeShortLE(DataOutputStream out, short value) throws IOException {
        out.writeByte(value & 0xFF);
        out.writeByte((value >>> 8) & 0xFF);
    }

    private static double randomSigned() {
        return RANDOM.nextDouble() * 2.0 - 1.0;
    }

    private static double clamp(double value, double low, double high) {
        return Math.max(low, Math.min(high, value));
    }

    private static double smoothstep(double x) {
        double clamped = clamp(x, 0.0, 1.0);
        return clamped * clamped * (3.0 - 2.0 * clamped);
    }
}
