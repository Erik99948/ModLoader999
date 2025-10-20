package com.example.modloader.api.world;

import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.block.Biome; // Import Biome

import java.util.Random;

public abstract class CustomChunkGenerator extends ChunkGenerator {

    /**
     * Generates the terrain for a chunk.
     * Mod developers should override this method to define their custom terrain.
     *
     * @param world The world the chunk belongs to.
     * @param random The random number generator for this chunk.
     * @param chunkX The X coordinate of the chunk.
     * @param chunkZ The Z coordinate of the chunk.
     * @param biome The biome grid for the chunk.
     * @return A ChunkData object representing the generated chunk.
     */
    @Override // This is the method mod developers will implement
    public abstract ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome);

    // Mod developers can optionally override other ChunkGenerator methods if needed.
    // For example, getDefaultPopulators, getDefaultBiomeProvider, etc.
}