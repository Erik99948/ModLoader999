package com.example.modloader.api;

/**
 * API for procedural generation including various noise functions.
 */
public interface ProceduralGenerationAPI {
    double perlinNoise2D(double x, double y, double frequency, int octaves, double lacunarity, double persistence, long seed);
    double perlinNoise3D(double x, double y, double z, double frequency, int octaves, double lacunarity, double persistence, long seed);
    double simplexNoise2D(double x, double y, double frequency, long seed);
    double simplexNoise3D(double x, double y, double z, double frequency, long seed);
    double voronoiNoise2D(double x, double y, double frequency, long seed);
    double ridgedNoise2D(double x, double y, double frequency, int octaves, double lacunarity, double persistence, long seed);
    double billowNoise2D(double x, double y, double frequency, int octaves, double lacunarity, double persistence, long seed);
    int generateHeight(int x, int z, int minHeight, int maxHeight, long seed);
    double[][] generateHeightMap(int width, int height, double frequency, int octaves, long seed);
}
