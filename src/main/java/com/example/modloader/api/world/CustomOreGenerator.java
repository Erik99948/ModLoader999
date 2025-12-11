package com.example.modloader.api.world;

import org.bukkit.World;
import org.bukkit.Chunk;

import java.util.Random;

public interface CustomOreGenerator {

    void generate(World world, Random random, int chunkX, int chunkZ);
}

