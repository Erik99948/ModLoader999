package com.example.examplemod;

import com.example.modloader.api.world.CustomChunkGenerator;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.Material;

import java.util.Random;

public class DarkOreChunkGenerator extends CustomChunkGenerator {

    private final BlockData darkOreBlockData;

    public DarkOreChunkGenerator(BlockData darkOreBlockData) {
        this.darkOreBlockData = darkOreBlockData;
    }

    @Override
    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biomeGrid) {
        ChunkData chunk = createChunkData(world);


        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = world.getMinHeight(); y < world.getMaxHeight(); y++) {
                    chunk.setBlock(x, y, z, Material.STONE);
                }
            }
        }


        if (random.nextDouble() < 0.10) {
            int veins = random.nextInt(3) + 1;
            for (int i = 0; i < veins; i++) {
                int x = random.nextInt(16);
                int z = random.nextInt(16);
                int y = random.nextInt(61);


                if (chunk.getBlockData(x, y, z).getMaterial() == Material.STONE) {
                    chunk.setBlock(x, y, z, darkOreBlockData);
                }
            }
        }

        return chunk;
    }
}
