package com.example.modloader.api;

import com.example.modloader.api.world.CustomChunkGenerator;
import org.bukkit.World;

public interface CustomWorldGeneratorAPI {

    boolean registerCustomChunkGenerator(String worldName, CustomChunkGenerator generator);

    CustomChunkGenerator getCustomChunkGenerator(String worldName);
}