package com.example.run;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Procedural Space Audio Synthesizer for Galaxy Striker.
 * Generates laser fire, explosions, and power-up sounds in real-time.
 */
public class SoundManager {
    private static final String TAG = "SoundManager";
    private static final int SAMPLE_RATE = 22050;
    private final ExecutorService soundExecutor = Executors.newFixedThreadPool(2);
    private boolean soundEnabled = true;

    public SoundManager() {
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public void toggleSound() {
        this.soundEnabled = !this.soundEnabled;
    }

    public void playLaserSound() {
        if (!soundEnabled) return;
        soundExecutor.execute(() -> {
            byte[] laserSweep = generateSweep(1200.0, 300.0, 60, 0.4f);
            playBuffer(laserSweep);
        });
    }

    public void playExplosionSound() {
        if (!soundEnabled) return;
        soundExecutor.execute(() -> {
            byte[] noise = generateNoise(180, 0.8f);
            playBuffer(noise);
        });
    }

    public void playPowerupSound() {
        if (!soundEnabled) return;
        soundExecutor.execute(() -> {
            byte[] t1 = generateTone(523.25, 45, 0.5f);
            byte[] t2 = generateTone(659.25, 45, 0.5f);
            byte[] t3 = generateTone(783.99, 45, 0.5f);
            byte[] t4 = generateTone(1046.50, 80, 0.6f);
            playBuffer(combineBuffers(t1, t2, t3, t4));
        });
    }

    public void playBombSound() {
        if (!soundEnabled) return;
        soundExecutor.execute(() -> {
            byte[] noise = generateNoise(350, 0.9f);
            byte[] sweep = generateSweep(300.0, 60.0, 350, 0.7f);
            playBuffer(combineBuffers(noise, sweep));
        });
    }

    private byte[] generateTone(double frequency, int durationMs, float volume) {
        int numSamples = (int) (SAMPLE_RATE * (durationMs / 1000.0));
        byte[] sample = new byte[numSamples];
        for (int i = 0; i < numSamples; i++) {
            double time = i / (double) SAMPLE_RATE;
            double envelope = 1.0 - ((double) i / numSamples);
            double wave = Math.sin(2.0 * Math.PI * frequency * time);
            short pcm = (short) (wave * 32767 * volume * envelope);
            sample[i] = (byte) (pcm >> 8);
        }
        return sample;
    }

    private byte[] generateSweep(double startFreq, double endFreq, int durationMs, float volume) {
        int numSamples = (int) (SAMPLE_RATE * (durationMs / 1000.0));
        byte[] sample = new byte[numSamples];
        for (int i = 0; i < numSamples; i++) {
            double progress = (double) i / numSamples;
            double currentFreq = startFreq + (endFreq - startFreq) * progress;
            double time = i / (double) SAMPLE_RATE;
            double envelope = 1.0 - progress;
            double wave = Math.sin(2.0 * Math.PI * currentFreq * time);
            short pcm = (short) (wave * 32767 * volume * envelope);
            sample[i] = (byte) (pcm >> 8);
        }
        return sample;
    }

    private byte[] generateNoise(int durationMs, float volume) {
        int numSamples = (int) (SAMPLE_RATE * (durationMs / 1000.0));
        byte[] sample = new byte[numSamples];
        for (int i = 0; i < numSamples; i++) {
            double envelope = 1.0 - ((double) i / numSamples);
            double randomVal = (Math.random() * 2.0) - 1.0;
            short pcm = (short) (randomVal * 32767 * volume * envelope);
            sample[i] = (byte) (pcm >> 8);
        }
        return sample;
    }

    private byte[] combineBuffers(byte[]... buffers) {
        int totalLen = 0;
        for (byte[] b : buffers) totalLen += b.length;
        byte[] result = new byte[totalLen];
        int pos = 0;
        for (byte[] b : buffers) {
            System.arraycopy(b, 0, result, pos, b.length);
            pos += b.length;
        }
        return result;
    }

    private void playBuffer(byte[] buffer) {
        try {
            AudioAttributes audioAttr = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            AudioFormat audioFormat = new AudioFormat.Builder()
                    .setSampleRate(SAMPLE_RATE)
                    .setEncoding(AudioFormat.ENCODING_PCM_8BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build();

            AudioTrack track = new AudioTrack(
                    audioAttr,
                    audioFormat,
                    buffer.length,
                    AudioTrack.MODE_STATIC,
                    AudioTrack.WRITE_BLOCKING
            );

            track.write(buffer, 0, buffer.length);
            track.play();

            new Thread(() -> {
                try {
                    Thread.sleep(buffer.length * 1000L / SAMPLE_RATE + 100);
                    track.stop();
                    track.release();
                } catch (Exception ignored) {
                }
            }).start();
        } catch (Exception e) {
            Log.e(TAG, "Error playing procedural sound", e);
        }
    }

    public void release() {
        soundExecutor.shutdown();
    }
}
