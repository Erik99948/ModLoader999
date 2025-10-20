package com.example.modloader.api;

import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import com.example.modloader.CustomCommandRegistry;

public interface ModAPI {

    void registerItem(String itemId, ItemStack item);

    void registerMob(com.example.modloader.CustomMob customMob);

    void registerBlock(com.example.modloader.CustomBlock customBlock);

    void registerCommand(String commandName, ModCommandExecutor executor);

    void registerListener(org.bukkit.event.Listener listener);

    void registerRecipe(org.bukkit.inventory.Recipe recipe);

    void registerWorldPopulator(CustomWorldPopulator populator, String[] worldNames, org.bukkit.block.Biome... biomes);

    void registerOreGenerator(com.example.modloader.api.world.CustomOreGenerator generator, String[] worldNames, org.bukkit.block.Biome... biomes);

    void registerTreeGenerator(com.example.modloader.api.world.CustomTreeGenerator generator, String[] worldNames, org.bukkit.block.Biome... biomes);

    void registerMobSpawner(com.example.modloader.api.mob.CustomMobSpawner spawner);

    void registerStructureGenerator(com.example.modloader.api.world.CustomStructureGenerator generator, String[] worldNames, org.bukkit.block.Biome... biomes);

    CustomCommandRegistry getCustomCommandRegistry();

    com.example.modloader.CustomMobRegistry getCustomMobRegistry();

    /**
     * Provides access to the Custom Inventory API for creating and managing custom GUIs.
     *
     * @return The CustomInventoryAPI instance.
     */
    CustomInventoryAPI getCustomInventoryAPI();

    /**
     * Provides access to the Custom Particle API for spawning custom particle effects.
     *
     * @return The CustomParticleAPI instance.
     */
    CustomParticleAPI getCustomParticleAPI();

    /**
     * Provides access to the Custom Sound API for playing custom sound effects.
     *
     * @return The CustomSoundAPI instance.
     */
    CustomSoundAPI getCustomSoundAPI();

    /**
     * Provides access to the Custom Enchantment API for registering and managing custom enchantments.
     *
     * @return The CustomEnchantmentAPI instance.
     */
    CustomEnchantmentAPI getCustomEnchantmentAPI();

    /**
     * Provides access to the Custom Potion Effect API for registering and managing custom potion effects.
     *
     * @return The CustomPotionEffectAPI instance.
     */
    CustomPotionEffectAPI getCustomPotionEffectAPI();

    /**
     * Provides access to the Custom World Generator API for registering and managing custom chunk generators.
     *
     * @return The CustomWorldGeneratorAPI instance.
     */
    CustomWorldGeneratorAPI getCustomWorldGeneratorAPI();

    /**
     * Provides access to the Dimension API for creating and managing worlds/dimensions.
     *
     * @return The DimensionAPI instance.
     */
    DimensionAPI getDimensionAPI();

    /**
     * Provides access to the Custom Structure API for loading and spawning custom structures.
     *
     * @return The CustomStructureAPI instance.
     */
    CustomStructureAPI getCustomStructureAPI();
}


