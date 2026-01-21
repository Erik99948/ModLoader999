package com.example.modloader.api;

import com.example.modloader.api.world.CustomChunkGenerator;
import com.example.modloader.api.world.CustomDimension;
import org.bukkit.World;

/**
 * API for dimension/world management.
 */
public interface DimensionAPI {
    World createWorld(String worldName, World.Environment environment);
    World createWorld(String worldName, World.Environment environment, CustomChunkGenerator generator);
    World createWorld(CustomDimension customDimension);
    World loadWorld(String worldName);
    boolean unloadWorld(String worldName, boolean save);
    World getWorld(String worldName);
    boolean registerCustomDimension(String dimensionId, CustomDimension customDimension);
    CustomDimension getCustomDimension(String dimensionId);
}
