package com.example.modloader.api;

import com.example.modloader.CustomStructureManager;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Random;

import java.util.concurrent.CompletableFuture;

public class CustomStructureAPIImpl implements CustomStructureAPI {

    private final CustomStructureManager structureManager;
    private final JavaPlugin plugin;

    public CustomStructureAPIImpl(JavaPlugin plugin) {
        this.plugin = plugin;
        this.structureManager = new CustomStructureManager(plugin);
    }

    @Override
    public boolean loadStructure(String structureId, File structureFile) {
        return structureManager.loadStructure(structureId, structureFile);
    }

    @Override
    public boolean spawnStructure(String structureId, Location location, Random random, int rotation, boolean mirror, float integrity) {
        return structureManager.spawnStructure(structureId, location, random, rotation, mirror, integrity);
    }

    @Override
    public CompletableFuture<Boolean> spawnStructureAsync(String structureId, Location location, Random random, int rotation, boolean mirror, float integrity) {
        return CompletableFuture.supplyAsync(() -> {
            return structureManager.spawnStructure(structureId, location, random, rotation, mirror, integrity);
        }, (runnable) -> plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable));
    }
}
