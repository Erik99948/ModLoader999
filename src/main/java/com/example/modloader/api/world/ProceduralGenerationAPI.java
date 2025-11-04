package com.example.modloader.api.world;

public interface ProceduralGenerationAPI {


    double generatePerlinNoise(double x, double y, double frequency, int octaves, double lacunarity, double persistence, long seed);


    double generatePerlinNoise(double x, double y, double z, double frequency, int octaves, double lacunarity, double persistence, long seed);


    double generateSimplexNoise(double x, double y, double frequency, long seed);


    double generateSimplexNoise(double x, double y, double z, double frequency, long seed);


}
