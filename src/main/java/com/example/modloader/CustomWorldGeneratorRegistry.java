package com.example.modloader;

import com.example.modloader.api.CustomWorldPopulator;
import com.example.modloader.api.world.CustomOreGenerator;
import com.example.modloader.api.world.CustomStructureGenerator;
import com.example.modloader.api.world.CustomTreeGenerator;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

public class CustomWorldGeneratorRegistry {

    private final Plugin plugin;
    private final Logger logger;

    // Global generators
    private final List<CustomWorldPopulator> globalPopulators = new ArrayList<>();
    private final List<CustomOreGenerator> globalOreGenerators = new ArrayList<>();
    private final List<CustomTreeGenerator> globalTreeGenerators = new ArrayList<>();
    private final List<CustomStructureGenerator> globalStructureGenerators = new ArrayList<>();

    // World-specific generators
    private final Map<String, List<CustomWorldPopulator>> worldSpecificPopulators = new HashMap<>();
    private final Map<String, List<CustomOreGenerator>> worldSpecificOreGenerators = new HashMap<>();
    private final Map<String, List<CustomTreeGenerator>> worldSpecificTreeGenerators = new HashMap<>();
    private final Map<String, List<CustomStructureGenerator>> worldSpecificStructureGenerators = new HashMap<>();

    // Biome-specific generators
    private final Map<Biome, List<CustomWorldPopulator>> biomeSpecificPopulators = new HashMap<>();
    private final Map<Biome, List<CustomOreGenerator>> biomeSpecificOreGenerators = new HashMap<>();
    private final Map<Biome, List<CustomTreeGenerator>> biomeSpecificTreeGenerators = new HashMap<>();
    private final Map<Biome, List<CustomStructureGenerator>> biomeSpecificStructureGenerators = new HashMap<>();

    public CustomWorldGeneratorRegistry(Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public void registerPopulator(CustomWorldPopulator populator, String[] worldNames, Biome... biomes) {
        if (worldNames != null && worldNames.length > 0) {
            for (String worldName : worldNames) {
                worldSpecificPopulators.computeIfAbsent(worldName, k -> new ArrayList<>()).add(populator);
                logger.info("Registered custom world populator for world '" + worldName + "': " + populator.getClass().getName());
            }
        } else if (biomes != null && biomes.length > 0) {
            for (Biome biome : biomes) {
                biomeSpecificPopulators.computeIfAbsent(biome, k -> new ArrayList<>()).add(populator);
                logger.info("Registered custom world populator for biome '" + biome.name() + "': " + populator.getClass().getName());
            }
        } else {
            globalPopulators.add(populator);
            logger.info("Registered global custom world populator: " + populator.getClass().getName());
        }
    }

    public void registerOreGenerator(CustomOreGenerator generator, String[] worldNames, Biome... biomes) {
        if (worldNames != null && worldNames.length > 0) {
            for (String worldName : worldNames) {
                worldSpecificOreGenerators.computeIfAbsent(worldName, k -> new ArrayList<>()).add(generator);
                logger.info("Registered custom ore generator for world '" + worldName + "': " + generator.getClass().getName());
            }
        } else if (biomes != null && biomes.length > 0) {
            for (Biome biome : biomes) {
                biomeSpecificOreGenerators.computeIfAbsent(biome, k -> new ArrayList<>()).add(generator);
                logger.info("Registered custom ore generator for biome '" + biome.name() + "': " + generator.getClass().getName());
            }
        } else {
            globalOreGenerators.add(generator);
            logger.info("Registered global custom ore generator: " + generator.getClass().getName());
        }
    }

    public void registerTreeGenerator(CustomTreeGenerator generator, String[] worldNames, Biome... biomes) {
        if (worldNames != null && worldNames.length > 0) {
            for (String worldName : worldNames) {
                worldSpecificTreeGenerators.computeIfAbsent(worldName, k -> new ArrayList<>()).add(generator);
                logger.info("Registered custom tree generator for world '" + worldName + "': " + generator.getClass().getName());
            }
        } else if (biomes != null && biomes.length > 0) {
            for (Biome biome : biomes) {
                biomeSpecificTreeGenerators.computeIfAbsent(biome, k -> new ArrayList<>()).add(generator);
                logger.info("Registered custom tree generator for biome '" + biome.name() + "': " + generator.getClass().getName());
            }
        } else {
            globalTreeGenerators.add(generator);
            logger.info("Registered global custom tree generator: " + generator.getClass().getName());
        }
    }

    public void registerStructureGenerator(CustomStructureGenerator generator, String[] worldNames, Biome... biomes) {
        if (worldNames != null && worldNames.length > 0) {
            for (String worldName : worldNames) {
                worldSpecificStructureGenerators.computeIfAbsent(worldName, k -> new ArrayList<>()).add(generator);
                logger.info("Registered custom structure generator for world '" + worldName + "': " + generator.getClass().getName());
            }
        } else if (biomes != null && biomes.length > 0) {
            for (Biome biome : biomes) {
                biomeSpecificStructureGenerators.computeIfAbsent(biome, k -> new ArrayList<>()).add(generator);
                logger.info("Registered custom structure generator for biome '" + biome.name() + "': " + generator.getClass().getName());
            }
        } else {
            globalStructureGenerators.add(generator);
            logger.info("Registered global custom structure generator: " + generator.getClass().getName());
        }
    }

    public void applyPopulatorsToWorld(World world) {
        String worldName = world.getName();

        // Apply global populators
        for (CustomWorldPopulator customPopulator : globalPopulators) {
            world.getPopulators().add(new CustomWorldPopulatorBlockPopulator(customPopulator));
            logger.info("Applied global populator " + customPopulator.getClass().getName() + " to world " + worldName);
        }
        // Apply world-specific populators
        if (worldSpecificPopulators.containsKey(worldName)) {
            for (CustomWorldPopulator customPopulator : worldSpecificPopulators.get(worldName)) {
                world.getPopulators().add(new CustomWorldPopulatorBlockPopulator(customPopulator));
                logger.info("Applied world-specific populator " + customPopulator.getClass().getName() + " to world " + worldName);
            }
        }
        // Apply biome-specific populators
        world.getPopulators().add(new BiomeSpecificWorldPopulatorBlockPopulator(biomeSpecificPopulators));

        // Apply global ore generators
        for (CustomOreGenerator generator : globalOreGenerators) {
            world.getPopulators().add(new CustomOreGeneratorBlockPopulator(generator));
        }
        // Apply world-specific ore generators
        if (worldSpecificOreGenerators.containsKey(worldName)) {
            for (CustomOreGenerator generator : worldSpecificOreGenerators.get(worldName)) {
                world.getPopulators().add(new CustomOreGeneratorBlockPopulator(generator));
            }
        }
        // Apply biome-specific ore generators
        world.getPopulators().add(new BiomeSpecificOreGeneratorBlockPopulator(biomeSpecificOreGenerators));

        // Apply global tree generators
        for (CustomTreeGenerator generator : globalTreeGenerators) {
            world.getPopulators().add(new CustomTreeGeneratorBlockPopulator(generator));
        }
        // Apply world-specific tree generators
        if (worldSpecificTreeGenerators.containsKey(worldName)) {
            for (CustomTreeGenerator generator : worldSpecificTreeGenerators.get(worldName)) {
                world.getPopulators().add(new CustomTreeGeneratorBlockPopulator(generator));
            }
        }
        // Apply biome-specific tree generators
        world.getPopulators().add(new BiomeSpecificTreeGeneratorBlockPopulator(biomeSpecificTreeGenerators));

        // Apply global structure generators
        for (CustomStructureGenerator generator : globalStructureGenerators) {
            world.getPopulators().add(new CustomStructureGeneratorBlockPopulator(generator));
        }
        // Apply world-specific structure generators
        if (worldSpecificStructureGenerators.containsKey(worldName)) {
            for (CustomStructureGenerator generator : worldSpecificStructureGenerators.get(worldName)) {
                world.getPopulators().add(new CustomStructureGeneratorBlockPopulator(generator));
            }
        }
        // Apply biome-specific structure generators
        world.getPopulators().add(new BiomeSpecificStructureGeneratorBlockPopulator(biomeSpecificStructureGenerators));
    }

    public void unregisterAll() {
        globalPopulators.clear();
        worldSpecificPopulators.clear();
        biomeSpecificPopulators.clear();
        globalOreGenerators.clear();
        worldSpecificOreGenerators.clear();
        biomeSpecificOreGenerators.clear();
        globalTreeGenerators.clear();
        worldSpecificTreeGenerators.clear();
        biomeSpecificTreeGenerators.clear();
        globalStructureGenerators.clear();
        worldSpecificStructureGenerators.clear();
        biomeSpecificStructureGenerators.clear();
        logger.info("Unregistered all custom world populator, ore, tree, and structure generator definitions.");
    }

    // Named inner classes for BlockPopulator implementations
    private static class CustomWorldPopulatorBlockPopulator extends BlockPopulator {
        private final CustomWorldPopulator customPopulator;

        public CustomWorldPopulatorBlockPopulator(CustomWorldPopulator customPopulator) {
            this.customPopulator = customPopulator;
        }

        @Override
        public void populate(World world, Random random, org.bukkit.Chunk chunk) {
            customPopulator.populate(world, random, chunk);
        }
    }

    private static class BiomeSpecificWorldPopulatorBlockPopulator extends BlockPopulator {
        private final Map<Biome, List<CustomWorldPopulator>> biomeSpecificPopulators;

        public BiomeSpecificWorldPopulatorBlockPopulator(Map<Biome, List<CustomWorldPopulator>> biomeSpecificPopulators) {
            this.biomeSpecificPopulators = biomeSpecificPopulators;
        }

        @Override
        public void populate(World world, Random random, org.bukkit.Chunk chunk) {
 Biome chunkBiome = world.getBiome(chunk.getX() * 16 + 7, 7, chunk.getZ() * 16 + 7); // Get biome at center of chunk
            if (biomeSpecificPopulators.containsKey(chunkBiome)) {
                for (CustomWorldPopulator customPopulator : biomeSpecificPopulators.get(chunkBiome)) {
                    customPopulator.populate(world, random, chunk);
                }
            }
        }
    }

    private static class CustomOreGeneratorBlockPopulator extends BlockPopulator {
        private final CustomOreGenerator generator;

        public CustomOreGeneratorBlockPopulator(CustomOreGenerator generator) {
            this.generator = generator;
        }

        @Override
        public void populate(World world, Random random, org.bukkit.Chunk chunk) {
            generator.generate(world, random, chunk.getX(), chunk.getZ());
        }
    }

    private static class BiomeSpecificOreGeneratorBlockPopulator extends BlockPopulator {
        private final Map<Biome, List<CustomOreGenerator>> biomeSpecificOreGenerators;

        public BiomeSpecificOreGeneratorBlockPopulator(Map<Biome, List<CustomOreGenerator>> biomeSpecificOreGenerators) {
            this.biomeSpecificOreGenerators = biomeSpecificOreGenerators;
        }

        @Override
        public void populate(World world, Random random, org.bukkit.Chunk chunk) {
 Biome chunkBiome = world.getBiome(chunk.getX() * 16 + 7, 7, chunk.getZ() * 16 + 7); // Get biome at center of chunk
            if (biomeSpecificOreGenerators.containsKey(chunkBiome)) {
                for (CustomOreGenerator generator : biomeSpecificOreGenerators.get(chunkBiome)) {
                    generator.generate(world, random, chunk.getX(), chunk.getZ());
                }
            }
        }
    }

    private static class CustomTreeGeneratorBlockPopulator extends BlockPopulator {
        private final CustomTreeGenerator generator;

        public CustomTreeGeneratorBlockPopulator(CustomTreeGenerator generator) {
            this.generator = generator;
        }

        @Override
        public void populate(World world, Random random, org.bukkit.Chunk chunk) {
            int x = chunk.getX() * 16 + random.nextInt(16);
            int z = chunk.getZ() * 16 + random.nextInt(16);
            int y = world.getHighestBlockYAt(x, z);
            generator.generate(world, random, x, y, z);
        }
    }

    private static class BiomeSpecificTreeGeneratorBlockPopulator extends BlockPopulator {
        private final Map<Biome, List<CustomTreeGenerator>> biomeSpecificTreeGenerators;

        public BiomeSpecificTreeGeneratorBlockPopulator(Map<Biome, List<CustomTreeGenerator>> biomeSpecificTreeGenerators) {
            this.biomeSpecificTreeGenerators = biomeSpecificTreeGenerators;
        }

        @Override
        public void populate(World world, Random random, org.bukkit.Chunk chunk) {
 Biome chunkBiome = world.getBiome(chunk.getX() * 16 + 7, 7, chunk.getZ() * 16 + 7); // Get biome at center of chunk
            if (biomeSpecificTreeGenerators.containsKey(chunkBiome)) {
                for (CustomTreeGenerator generator : biomeSpecificTreeGenerators.get(chunkBiome)) {
                    int x = chunk.getX() * 16 + random.nextInt(16);
                    int z = chunk.getZ() * 16 + random.nextInt(16);
                    int y = world.getHighestBlockYAt(x, z);
                    generator.generate(world, random, x, y, z);
                }
            }
        }
    }

    private static class CustomStructureGeneratorBlockPopulator extends BlockPopulator {
        private final CustomStructureGenerator generator;

        public CustomStructureGeneratorBlockPopulator(CustomStructureGenerator generator) {
            this.generator = generator;
        }

        @Override
        public void populate(World world, Random random, org.bukkit.Chunk chunk) {
            generator.generate(world, random, chunk.getX(), chunk.getZ());
        }
    }

    private static class BiomeSpecificStructureGeneratorBlockPopulator extends BlockPopulator {
        private final Map<Biome, List<CustomStructureGenerator>> biomeSpecificStructureGenerators;

        public BiomeSpecificStructureGeneratorBlockPopulator(Map<Biome, List<CustomStructureGenerator>> biomeSpecificStructureGenerators) {
            this.biomeSpecificStructureGenerators = biomeSpecificStructureGenerators;
        }

        @Override
        public void populate(World world, Random random, org.bukkit.Chunk chunk) {
 Biome chunkBiome = world.getBiome(chunk.getX() * 16 + 7, 7, chunk.getZ() * 16 + 7); // Get biome at center of chunk
            if (biomeSpecificStructureGenerators.containsKey(chunkBiome)) {
                for (CustomStructureGenerator generator : biomeSpecificStructureGenerators.get(chunkBiome)) {
                    generator.generate(world, random, chunk.getX(), chunk.getZ());
                }
            }
        }
    }
}
