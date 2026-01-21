package com.example.modloader.api.world;

import org.bukkit.block.Biome;

public class CustomBiome {
    private final String id;
    private final String name;
    private final Biome baseBiome;
    private float temperature;
    private float humidity;
    private int skyColor;
    private int fogColor;
    private int waterColor;
    private int grassColor;
    private int foliageColor;

    public CustomBiome(String id, String name, Biome baseBiome) {
        this.id = id;
        this.name = name;
        this.baseBiome = baseBiome;
        this.temperature = 0.8f;
        this.humidity = 0.4f;
        this.skyColor = 0x87CEEB;
        this.fogColor = 0xC0C0C0;
        this.waterColor = 0x3F76E4;
        this.grassColor = 0x7CBD6B;
        this.foliageColor = 0x48B518;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public Biome getBaseBiome() { return baseBiome; }
    public float getTemperature() { return temperature; }
    public void setTemperature(float temperature) { this.temperature = temperature; }
    public float getHumidity() { return humidity; }
    public void setHumidity(float humidity) { this.humidity = humidity; }
    public int getSkyColor() { return skyColor; }
    public void setSkyColor(int skyColor) { this.skyColor = skyColor; }
    public int getFogColor() { return fogColor; }
    public void setFogColor(int fogColor) { this.fogColor = fogColor; }
    public int getWaterColor() { return waterColor; }
    public void setWaterColor(int waterColor) { this.waterColor = waterColor; }
    public int getGrassColor() { return grassColor; }
    public void setGrassColor(int grassColor) { this.grassColor = grassColor; }
    public int getFoliageColor() { return foliageColor; }
    public void setFoliageColor(int foliageColor) { this.foliageColor = foliageColor; }
}
