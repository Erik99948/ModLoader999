package com.example.modloader.api.world;

import org.bukkit.World;

public class CustomDimension {
    private final String id;
    private final String name;
    private final World.Environment environment;
    private final long seed;
    private boolean hardcore;
    private CustomChunkGenerator chunkGenerator;

    public CustomDimension(String id, String name, World.Environment environment, long seed) {
        this.id = id;
        this.name = name;
        this.environment = environment;
        this.seed = seed;
        this.hardcore = false;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public World.Environment getEnvironment() { return environment; }
    public long getSeed() { return seed; }
    public boolean isHardcore() { return hardcore; }
    public void setHardcore(boolean hardcore) { this.hardcore = hardcore; }
    public CustomChunkGenerator getChunkGenerator() { return chunkGenerator; }
    public void setChunkGenerator(CustomChunkGenerator chunkGenerator) { this.chunkGenerator = chunkGenerator; }
}
