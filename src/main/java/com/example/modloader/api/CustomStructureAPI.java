package com.example.modloader.api;

import org.bukkit.Location;
import java.io.File;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

/**
 * API for structure generation.
 */
public interface CustomStructureAPI {
    boolean loadStructure(String structureId, File structureFile);
    boolean spawnStructure(String structureId, Location location, Random random, int rotation, boolean mirror, float integrity);
    CompletableFuture<Boolean> spawnStructureAsync(String structureId, Location location, Random random, int rotation, boolean mirror, float integrity);
}
