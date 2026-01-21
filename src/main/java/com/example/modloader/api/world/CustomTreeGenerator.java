package com.example.modloader.api.world;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import java.util.Random;

public interface CustomTreeGenerator {
    boolean generate(World world, Random random, Location location);
    int getMinHeight();
    int getMaxHeight();
    Biome[] getValidBiomes();
}
