package com.example.modloader.api;

import com.example.modloader.api.world.CustomChunkGenerator;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;

public interface DimensionAPI {

    World createWorld(String worldName, Environment environment);

    World createWorld(String worldName, Environment environment, CustomChunkGenerator generator);

    World loadWorld(String worldName);

    boolean unloadWorld(String worldName, boolean save);

    World getWorld(String worldName);
}