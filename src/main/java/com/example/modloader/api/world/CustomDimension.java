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

    // Custom properties for physics, sky, fog, etc.
    // These would likely be implemented by custom ChunkGenerators or event listeners
    // For example, a custom gravity setting, or custom sky color

    // Example: Custom gravity factor (1.0 for normal gravity)
    double getGravityFactor();

    // Example: Custom sky color (RGB hex string or Color object)
    String getSkyColorHex();

    // Example: Custom fog color
    String getFogColorHex();

    // Example: Custom ChunkGenerator for this dimension
    ChunkGenerator getChunkGenerator();

    // Other properties as needed
}
