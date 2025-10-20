package com.example.modloader.api;

import com.example.modloader.api.world.CustomChunkGenerator;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;

public interface DimensionAPI {

    /**
     * Creates a new world (dimension) with the specified name and environment.
     *
     * @param worldName The name of the new world.
     * @param environment The environment type of the world (NORMAL, NETHER, THE_END).
     * @return The newly created World object, or null if creation failed.
     */
    World createWorld(String worldName, Environment environment);

    /**
     * Creates a new world (dimension) with the specified name, environment, and custom chunk generator.
     *
     * @param worldName The name of the new world.
     * @param environment The environment type of the world (NORMAL, NETHER, THE_END).
     * @param generator The custom chunk generator to use for this world.
     * @return The newly created World object, or null if creation failed.
     */
    World createWorld(String worldName, Environment environment, CustomChunkGenerator generator);

    /**
     * Loads an existing world.
     *
     * @param worldName The name of the world to load.
     * @return The loaded World object, or null if loading failed or world does not exist.
     */
    World loadWorld(String worldName);

    /**
     * Unloads a world.
     *
     * @param worldName The name of the world to unload.
     * @param save Whether to save the world before unloading.
     * @return true if the world was successfully unloaded, false otherwise.
     */
    boolean unloadWorld(String worldName, boolean save);

    /**
     * Gets an existing world by its name.
     *
     * @param worldName The name of the world.
     * @return The World object if found, null otherwise.
     */
    World getWorld(String worldName);
}