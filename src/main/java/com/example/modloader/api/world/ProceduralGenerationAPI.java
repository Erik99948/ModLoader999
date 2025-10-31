package com.example.modloader.api.world;

public interface ProceduralGenerationAPI {

    /**
     * Generates 2D Perlin noise.
     * @param x X coordinate
     * @param y Y coordinate
     * @param frequency The frequency of the noise. Higher values result in more detailed noise.
     * @param octaves The number of noise layers to combine. Higher values add more detail.
     * @param lacunarity The frequency multiplier between successive octaves. Typically 2.0.
     * @param persistence The amplitude multiplier between successive octaves. Typically 0.5.
     * @param seed The seed for the noise generator.
     * @return A noise value between -1.0 and 1.0.
     */
    double generatePerlinNoise(double x, double y, double frequency, int octaves, double lacunarity, double persistence, long seed);

    /**
     * Generates 3D Perlin noise.
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @param frequency The frequency of the noise. Higher values result in more detailed noise.
     * @param octaves The number of noise layers to combine. Higher values add more detail.
     * @param lacunarity The frequency multiplier between successive octaves. Typically 2.0.
     * @param persistence The amplitude multiplier between successive octaves. Typically 0.5.
     * @param seed The seed for the noise generator.
     * @return A noise value between -1.0 and 1.0.
     */
    double generatePerlinNoise(double x, double y, double z, double frequency, int octaves, double lacunarity, double persistence, long seed);

    /**
     * Generates 2D Simplex noise.
     * @param x X coordinate
     * @param y Y coordinate
     * @param frequency The frequency of the noise.
     * @param seed The seed for the noise generator.
     * @return A noise value between -1.0 and 1.0.
     */
    double generateSimplexNoise(double x, double y, double frequency, long seed);

    /**
     * Generates 3D Simplex noise.
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @param frequency The frequency of the noise.
     * @param seed The seed for the noise generator.
     * @return A noise value between -1.0 and 1.0.
     */
    double generateSimplexNoise(double x, double y, double z, double frequency, long seed);

    // Add more procedural generation utilities as needed (e.g., cellular automata, fractal generation)
}
