package com.example.modloader.api;

import com.example.modloader.api.world.CustomChunkGenerator;
import org.bukkit.World;

public interface CustomWorldGeneratorAPI {

    /**
     * Registers a custom chunk generator for a specific world.
     * This generator will be used when the specified world is created or loaded.
     *
     * @param worldName The name of the world to associate the generator with.
     * @param generator The custom chunk generator to register.
     * @return true if the generator was registered successfully, false otherwise.
     */
    boolean registerCustomChunkGenerator(String worldName, CustomChunkGenerator generator);

    /**
     * Gets the custom chunk generator registered for a specific world.
     *
     * @param worldName The name of the world.
     * @return The CustomChunkGenerator if registered, null otherwise.
     */
    CustomChunkGenerator getCustomChunkGenerator(String worldName);
}