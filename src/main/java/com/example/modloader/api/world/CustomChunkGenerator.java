package com.example.modloader.api.world;

import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import java.util.Random;

public abstract class CustomChunkGenerator extends ChunkGenerator {
    protected final long seed;
    
    public CustomChunkGenerator(long seed) {
        this.seed = seed;
    }
    
    public long getSeed() {
        return seed;
    }
    
    @Override
    @SuppressWarnings("deprecation")
    public abstract ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome);
}
