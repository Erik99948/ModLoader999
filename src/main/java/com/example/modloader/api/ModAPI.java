package com.example.modloader.api;

import com.example.modloader.api.event.EventBus;
import com.example.modloader.api.gui.GUIAPI;
import com.example.modloader.api.mob.CustomMobSpawner;
import com.example.modloader.api.network.Networking;
import com.example.modloader.api.permissions.Permissions;
import com.example.modloader.api.world.*;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Main API interface for mod development.
 * Provides access to all modding capabilities.
 */
public interface ModAPI {
    // ==================== Registration Methods ====================
    void registerItem(String itemId, ItemStack item);
    void registerMob(Object customMob);
    void registerBlock(Object customBlock);
    void registerCommand(String commandName, ModCommandExecutor executor);
    void registerListener(org.bukkit.event.Listener listener);
    void registerEventListener(Object listenerObject);
    void registerRecipe(Recipe recipe);
    void registerWorldPopulator(CustomWorldPopulator populator, String[] worldNames, Biome... biomes);
    void registerOreGenerator(CustomOreGenerator generator, String[] worldNames, Biome... biomes);
    void registerTreeGenerator(CustomTreeGenerator generator, String[] worldNames, Biome... biomes);
    void registerMobSpawner(CustomMobSpawner spawner);
    void registerStructureGenerator(CustomStructureGenerator generator, String[] worldNames, Biome... biomes);

    // ==================== Registry Access ====================
    Object getCustomCommandRegistry();
    Object getCustomMobRegistry();

    // ==================== API Access ====================
    CustomInventoryAPI getCustomInventoryAPI();
    CustomParticleAPI getCustomParticleAPI();
    CustomSoundAPI getCustomSoundAPI();
    CustomEnchantmentAPI getCustomEnchantmentAPI();
    CustomPotionEffectAPI getCustomPotionEffectAPI();
    CustomWorldGeneratorAPI getCustomWorldGeneratorAPI();
    DimensionAPI getDimensionAPI();
    CustomStructureAPI getCustomStructureAPI();
    CustomAssetAPI getCustomAssetAPI();
    ModMessageAPI getModMessageAPI();
    EventBus getEventBus();
    Permissions getPermissions();
    Networking getNetworking();
    GUIAPI getGUIAPI();
    VoiceAPI getVoiceAPI();
    ProceduralGenerationAPI getProceduralGenerationAPI();
    JavaPlugin getPlugin();

    // ==================== Config ====================
    <T extends com.example.modloader.api.config.ModConfig> T getModConfig(Class<T> configClass);
    <T extends com.example.modloader.api.config.ModConfig> T getModConfig(String modId, Class<T> configClass);
}
