package com.example.modloader.api.world;

import org.bukkit.World;
import org.bukkit.block.Biome;
import java.util.Random;

public interface CustomStructureGenerator {
    boolean generate(World world, Random random, int chunkX, int chunkZ);
    double getSpawnChance();
    int getMinDistance();
    Biome[] getValidBiomes();
}
