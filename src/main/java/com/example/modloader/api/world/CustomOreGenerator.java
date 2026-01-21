package com.example.modloader.api.world;

import org.bukkit.World;
import org.bukkit.block.Biome;
import java.util.Random;

public interface CustomOreGenerator {
    void generate(World world, Random random, int chunkX, int chunkZ);
    int getMinY();
    int getMaxY();
    int getVeinSize();
    double getSpawnChance();
    Biome[] getValidBiomes();
}
