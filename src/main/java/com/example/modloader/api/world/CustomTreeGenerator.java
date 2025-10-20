package com.example.modloader.api.world;

import org.bukkit.World;

import java.util.Random;

public interface CustomTreeGenerator {

    boolean generate(World world, Random random, int x, int y, int z);
}
