package com.example.modloader;

import com.example.modloader.api.CustomWorldPopulator;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import java.util.Random;

public class CustomOreWorldPopulator implements CustomWorldPopulator {

    private final BlockData oreBlockData;
    private final int minY;
    private final int maxY;
    private final double chancePerChunk;
    private final int minVeinSize;
    private final int maxVeinSize;

    public CustomOreWorldPopulator(BlockData oreBlockData, int minY, int maxY, double chancePerChunk, int minVeinSize, int maxVeinSize) {
        this.oreBlockData = oreBlockData;
        this.minY = minY;
        this.maxY = maxY;
        this.chancePerChunk = chancePerChunk;
        this.minVeinSize = minVeinSize;
        this.maxVeinSize = maxVeinSize;
    }

    @Override
    public void populate(World world, Random random, Chunk chunk) {
        if (random.nextDouble() < chancePerChunk) {
            int veins = random.nextInt(maxVeinSize - minVeinSize + 1) + minVeinSize;
            for (int i = 0; i < veins; i++) {
                int x = random.nextInt(16);
                int z = random.nextInt(16);
                int y = random.nextInt(maxY - minY + 1) + minY;

                Block block = chunk.getBlock(x, y, z);
                if (block.getType() == Material.STONE || block.getType() == Material.GRANITE ||
                    block.getType() == Material.DIORITE || block.getType() == Material.ANDESITE ||
                    block.getType() == Material.DEEPSLATE) {
                    block.setBlockData(oreBlockData);
                }
            }
        }
    }
}
