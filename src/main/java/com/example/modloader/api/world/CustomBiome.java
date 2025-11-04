package com.example.modloader.api.world;

import org.bukkit.block.Biome;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Color;

public interface CustomBiome {

    String getId();

    String getName();

    Biome getBaseBiome();


    Color getSkyColor();
    Color getFogColor();
    Color getWaterColor();
    Color getWaterFogColor();
    Color getGrassColor();
    Color getFoliageColor();


    Particle getAmbientParticle();
    int getAmbientParticleCount();
    double getAmbientParticleChance();


    Sound getAmbientSound();
    double getAmbientSoundVolume();
    double getAmbientSoundPitch();


    float getTemperature();
    float getHumidity();


    boolean hasPrecipitation();




}
