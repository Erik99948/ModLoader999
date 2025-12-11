package com.example.modloader.api.world;

import org.bukkit.plugin.java.JavaPlugin;

public class ProceduralGenerationAPIImpl implements ProceduralGenerationAPI {

    private final JavaPlugin plugin;

    public ProceduralGenerationAPIImpl(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    private double hash(long seed, double x, double y) {
        long h = seed;
        h = 31 * h + Double.doubleToLongBits(x);
        h = 31 * h + Double.doubleToLongBits(y);
        return (double) (new java.util.Random(h).nextDouble() * 2.0 - 1.0);
    }

    private double hash(long seed, double x, double y, double z) {
        long h = seed;
        h = 31 * h + Double.doubleToLongBits(x);
        h = 31 * h + Double.doubleToLongBits(y);
        h = 31 * h + Double.doubleToLongBits(z);
        return (double) (new java.util.Random(h).nextDouble() * 2.0 - 1.0);
    }

    @Override
    public double generatePerlinNoise(double x, double y, double frequency, int octaves, double lacunarity, double persistence, long seed) {
        double total = 0;
        double maxAmplitude = 0;
        double amplitude = 1;
        double freq = frequency;

        for (int i = 0; i < octaves; i++) {
            total += hash(seed, x * freq, y * freq) * amplitude;
            maxAmplitude += amplitude;
            amplitude *= persistence;
            freq *= lacunarity;
        }

        return total / maxAmplitude;
    }

    @Override
    public double generatePerlinNoise(double x, double y, double z, double frequency, int octaves, double lacunarity, double persistence, long seed) {
        double total = 0;
        double maxAmplitude = 0;
        double amplitude = 1;
        double freq = frequency;

        for (int i = 0; i < octaves; i++) {
            total += hash(seed, x * freq, y * freq, z * freq) * amplitude;
            maxAmplitude += amplitude;
            amplitude *= persistence;
            freq *= lacunarity;
        }

        return total / maxAmplitude;
    }

    @Override
    public double generateSimplexNoise(double x, double y, double frequency, long seed) {
        return hash(seed, x * frequency, y * frequency);
    }

    @Override
    public double generateSimplexNoise(double x, double y, double z, double frequency, long seed) {
        return hash(seed, x * frequency, y * frequency, z * frequency);
    }
}
