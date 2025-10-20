package com.example.modloader.api.mob;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;

import java.util.List;
import java.util.Random;

public interface CustomMobSpawner {

    List<LivingEntity> spawn(World world, Random random, Block block);
}
