package com.example.modloader.api;

import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Random;

public interface CustomStructureAPI {

    /**
     * Loads a structure from a file (e.g., an .nbt file) and registers it with a given ID.
     *
     * @param structureId The unique ID for this structure.
     * @param structureFile The file containing the structure data.
     * @return true if the structure was loaded and registered successfully, false otherwise.
     */
    boolean loadStructure(String structureId, File structureFile);

    /**
     * Spawns a loaded structure at a specific location in the world.
     *
     * @param structureId The ID of the structure to spawn.
     * @param location The base location to spawn the structure.
     * @param random A random instance for potential random elements (e.g., rotation, integrity).
     * @param rotation The rotation of the structure (0, 90, 180, 270 degrees).
     * @param mirror Whether to mirror the structure.
     * @param integrity The integrity of the structure (0.0 to 1.0, for partial generation).
     * @return true if the structure was spawned successfully, false if not found.
     */
    boolean spawnStructure(String structureId, Location location, Random random, int rotation, boolean mirror, float integrity);
}