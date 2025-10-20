package com.example.modloader.api;

import org.bukkit.World;
import org.bukkit.Chunk;

import java.util.Random;

public interface CustomWorldPopulator {

    void populate(World world, Random random, Chunk chunk);
}
