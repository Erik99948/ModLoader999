package com.example.modloader.api;

import com.example.modloader.api.world.CustomChunkGenerator;
import com.example.modloader.api.world.CustomDimension;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class DimensionAPIImpl implements DimensionAPI {

    private final JavaPlugin plugin;
    private final Map<String, CustomDimension> registeredDimensions = new HashMap<>();

    public DimensionAPIImpl(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public World createWorld(String worldName, Environment environment) {
        return createWorld(worldName, environment, null);
    }

    @Override
    public World createWorld(String worldName, Environment environment, CustomChunkGenerator generator) {
        WorldCreator creator = new WorldCreator(worldName);
        creator.environment(environment);
        if (generator != null) {
            creator.generator(generator);
        }

        World world = creator.createWorld();
        if (world != null) {
            plugin.getLogger().info("Created world: " + worldName + " with environment: " + environment.name());
        } else {
            plugin.getLogger().warning("Failed to create world: " + worldName);
        }
        return world;
    }

    @Override
    public World createWorld(CustomDimension customDimension) {
        WorldCreator creator = new WorldCreator(customDimension.getName());
        creator.environment(customDimension.getEnvironment());
        creator.seed(customDimension.getSeed());
        creator.hardcore(customDimension.isHardcore());
        if (customDimension.getChunkGenerator() != null) {
            creator.generator(customDimension.getChunkGenerator());
        }

        World world = creator.createWorld();
        if (world != null) {
            plugin.getLogger().info("Created custom world: " + customDimension.getName() + " with environment: " + customDimension.getEnvironment().name());
        } else {
            plugin.getLogger().warning("Failed to create custom world: " + customDimension.getName());
        }
        return world;
    }

    @Override
    public World loadWorld(String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            plugin.getLogger().info("World '" + worldName + "' is already loaded.");
            return world;
        }

        WorldCreator creator = new WorldCreator(worldName);
world = creator.createWorld();
        if (world != null) {
            plugin.getLogger().info("Loaded world: " + worldName);
        } else {
            plugin.getLogger().warning("Failed to load world: " + worldName);
        }
        return world;
    }

    @Override
    public boolean unloadWorld(String worldName, boolean save) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            plugin.getLogger().warning("Attempted to unload world '" + worldName + "', but it is not loaded.");
            return false;
        }

        boolean success = Bukkit.unloadWorld(world, save);
        if (success) {
            plugin.getLogger().info("Unloaded world: " + worldName + " (saved: " + save + ")");
        } else {
            plugin.getLogger().warning("Failed to unload world: " + worldName);
        }
        return success;
    }

    @Override
    public World getWorld(String worldName) {
        return Bukkit.getWorld(worldName);
    }

    @Override
    public boolean registerCustomDimension(String dimensionId, CustomDimension customDimension) {
        if (registeredDimensions.containsKey(dimensionId.toLowerCase())) {
            plugin.getLogger().warning("Attempted to register a custom dimension for an already registered ID: " + dimensionId);
            return false;
        }
        registeredDimensions.put(dimensionId.toLowerCase(), customDimension);
        plugin.getLogger().info("Registered custom dimension: " + dimensionId);
        return true;
    }

    @Override
    public CustomDimension getCustomDimension(String dimensionId) {
        return registeredDimensions.get(dimensionId.toLowerCase());
    }
}
