package com.example.modloader.api.world;

import org.bukkit.block.Biome;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Color;

public interface CustomBiome {

    String getId();

    String getName();

    Biome getBaseBiome();

    // Visual properties
    Color getSkyColor();
    Color getFogColor();
    Color getWaterColor();
    Color getWaterFogColor();
    Color getGrassColor();
    Color getFoliageColor();

    // Particle properties
    Particle getAmbientParticle();
    int getAmbientParticleCount();
    double getAmbientParticleChance();

    // Sound properties
    Sound getAmbientSound();
    double getAmbientSoundVolume();
    double getAmbientSoundPitch();

    // Temperature and Humidity
    float getTemperature();
    float getHumidity();

    // Precipitation
    boolean hasPrecipitation();

    // Surface Builder (more advanced, might need a separate API)
    // BlockData getSurfaceBlock();
    // BlockData getGroundBlock();
}
