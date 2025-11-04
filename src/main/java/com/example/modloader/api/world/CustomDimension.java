package com.example.modloader.api.world;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.generator.ChunkGenerator;

public interface CustomDimension {

    String getId();

    String getName();

    Environment getEnvironment();

    long getSeed();

    boolean isHardcore();

    boolean hasStorm();

    boolean isThundering();

    long getFullTime();






    double getGravityFactor();


    String getSkyColorHex();


    String getFogColorHex();


    ChunkGenerator getChunkGenerator();


}
