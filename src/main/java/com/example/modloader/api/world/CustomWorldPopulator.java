package com.example.modloader.api.world;

import org.bukkit.Chunk;
import org.bukkit.World;
import java.util.Random;

public interface CustomWorldPopulator {
    void populate(World world, Random random, Chunk chunk);
}
