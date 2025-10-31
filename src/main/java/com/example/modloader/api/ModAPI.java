package com.example.modloader.api;

import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import com.example.modloader.CustomCommandRegistry;
import com.example.modloader.api.event.EventBus;
import com.example.modloader.api.permissions.Permissions;
import com.example.modloader.api.network.Networking;

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

    CustomInventoryAPI getCustomInventoryAPI();

    CustomParticleAPI getCustomParticleAPI();

    CustomSoundAPI getCustomSoundAPI();

    CustomEnchantmentAPI getCustomEnchantmentAPI();

    CustomPotionEffectAPI getCustomPotionEffectAPI();

    CustomWorldGeneratorAPI getCustomWorldGeneratorAPI();

    DimensionAPI getDimensionAPI();

    CustomStructureAPI getCustomStructureAPI();

    <T extends com.example.modloader.api.config.ModConfig> T getModConfig(Class<T> configClass);

    ModMessageAPI getModMessageAPI();

    CustomAssetAPI getCustomAssetAPI();
    
    EventBus getEventBus();

    Permissions getPermissions();

    Networking getNetworking();

    com.example.modloader.api.gui.GUIAPI getGUIAPI();

    com.example.modloader.api.world.ProceduralGenerationAPI getProceduralGenerationAPI();
}