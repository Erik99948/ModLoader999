package com.example.modloader.api.world;

import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.block.Biome;

import java.util.Random;

public abstract class CustomChunkGenerator extends ChunkGenerator {

    @Override
    public abstract ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome);
}