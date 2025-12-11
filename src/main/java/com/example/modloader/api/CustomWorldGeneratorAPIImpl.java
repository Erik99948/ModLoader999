package com.example.modloader.api;

import com.example.modloader.api.world.CustomBiome;
import com.example.modloader.api.world.CustomChunkGenerator;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class CustomWorldGeneratorAPIImpl implements CustomWorldGeneratorAPI {

    private final JavaPlugin plugin;
    private final Map<String, CustomChunkGenerator> registeredGenerators = new HashMap<>();
    private final Map<String, CustomBiome> registeredBiomes = new HashMap<>();

    public CustomWorldGeneratorAPIImpl(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean registerCustomChunkGenerator(String worldName, CustomChunkGenerator generator) {
        if (registeredGenerators.containsKey(worldName.toLowerCase())) {
            plugin.getLogger().warning("Attempted to register a custom chunk generator for an already registered world: " + worldName);
            return false;
        }
        registeredGenerators.put(worldName.toLowerCase(), generator);
        plugin.getLogger().info("Registered custom chunk generator for world: " + worldName);
        return true;
    }

    @Override
    public CustomChunkGenerator getCustomChunkGenerator(String worldName) {
        return registeredGenerators.get(worldName.toLowerCase());
    }

    @Override
    public boolean registerCustomBiome(String biomeId, CustomBiome biome) {
        if (registeredBiomes.containsKey(biomeId.toLowerCase())) {
            plugin.getLogger().warning("Attempted to register a custom biome for an already registered ID: " + biomeId);
            return false;
        }
        registeredBiomes.put(biomeId.toLowerCase(), biome);
        plugin.getLogger().info("Registered custom biome: " + biomeId);
        return true;
    }

    @Override
    public CustomBiome getCustomBiome(String biomeId) {
        return registeredBiomes.get(biomeId.toLowerCase());
    }

    public void unregisterAll() {
        registeredGenerators.clear();
        registeredBiomes.clear();
        plugin.getLogger().info("Unregistered all custom chunk generators and biomes.");
    }
}
