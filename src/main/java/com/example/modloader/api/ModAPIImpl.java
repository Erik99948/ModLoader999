package com.example.modloader.api;

import com.example.modloader.CustomBlockRegistry;
import com.example.modloader.CustomCommandRegistry;
import com.example.modloader.CustomEventListenerRegistry;
import com.example.modloader.CustomItemRegistry;
import com.example.modloader.CustomMobRegistry;
import com.example.modloader.CustomRecipeRegistry;
import com.example.modloader.CustomWorldGeneratorRegistry;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;

public class ModAPIImpl implements ModAPI {

    private final CustomItemRegistry itemRegistry;
    private final CustomMobRegistry mobRegistry;
    private final CustomBlockRegistry blockRegistry;
    private final CustomCommandRegistry commandRegistry;
    private final CustomEventListenerRegistry eventListenerRegistry;
    private final CustomRecipeRegistry recipeRegistry;
    private final CustomWorldGeneratorRegistry worldGeneratorRegistry;
    private final CustomInventoryAPI customInventoryAPI;
    private final CustomParticleAPI customParticleAPI;
    private final CustomSoundAPI customSoundAPI;
    private final CustomEnchantmentAPI customEnchantmentAPI;
    private final CustomPotionEffectAPI customPotionEffectAPI;
    private final CustomWorldGeneratorAPI customWorldGeneratorAPI;
    private final DimensionAPI dimensionAPI;
    private final CustomStructureAPI customStructureAPI;

    public ModAPIImpl(JavaPlugin plugin, CustomItemRegistry itemRegistry, CustomMobRegistry mobRegistry, CustomBlockRegistry blockRegistry, CustomCommandRegistry commandRegistry, CustomEventListenerRegistry eventListenerRegistry, CustomRecipeRegistry recipeRegistry, CustomWorldGeneratorRegistry worldGeneratorRegistry) {
        this.itemRegistry = itemRegistry;
        this.mobRegistry = mobRegistry;
        this.blockRegistry = blockRegistry;
        this.commandRegistry = commandRegistry;
        this.eventListenerRegistry = eventListenerRegistry;
        this.recipeRegistry = recipeRegistry;
        this.worldGeneratorRegistry = worldGeneratorRegistry;
        this.customInventoryAPI = new CustomInventoryAPIImpl(plugin);
        this.customParticleAPI = new CustomParticleAPIImpl();
        this.customSoundAPI = new CustomSoundAPIImpl();
        this.customEnchantmentAPI = new CustomEnchantmentAPIImpl(plugin);
        this.customPotionEffectAPI = new CustomPotionEffectAPIImpl(plugin);
        this.customWorldGeneratorAPI = new CustomWorldGeneratorAPIImpl(plugin);
        this.dimensionAPI = new DimensionAPIImpl(plugin);
        this.customStructureAPI = new CustomStructureAPIImpl(plugin);
    }

    @Override
    public void registerItem(String itemId, ItemStack item) {
        itemRegistry.register(itemId, item);
    }

    @Override
    public void registerMob(com.example.modloader.CustomMob customMob) {
        mobRegistry.register(customMob);
    }

    @Override
    public void registerBlock(com.example.modloader.CustomBlock customBlock) {
        blockRegistry.register(customBlock);
    }

    @Override
    public void registerCommand(String commandName, ModCommandExecutor executor) {
        commandRegistry.register(commandName, executor);
    }

    @Override
    public void registerListener(Listener listener) {
        eventListenerRegistry.register(listener);
    }

    @Override
    public void registerRecipe(Recipe recipe) {
        recipeRegistry.register(recipe);
    }

    @Override
    public void registerWorldPopulator(CustomWorldPopulator populator, String[] worldNames, org.bukkit.block.Biome... biomes) {
        worldGeneratorRegistry.registerPopulator(populator, worldNames, biomes);
    }

    @Override
    public void registerOreGenerator(com.example.modloader.api.world.CustomOreGenerator generator, String[] worldNames, org.bukkit.block.Biome... biomes) {
        worldGeneratorRegistry.registerOreGenerator(generator, worldNames, biomes);
    }

    @Override
    public void registerTreeGenerator(com.example.modloader.api.world.CustomTreeGenerator generator, String[] worldNames, org.bukkit.block.Biome... biomes) {
        worldGeneratorRegistry.registerTreeGenerator(generator, worldNames, biomes);
    }

    @Override
    public void registerStructureGenerator(com.example.modloader.api.world.CustomStructureGenerator generator, String[] worldNames, org.bukkit.block.Biome... biomes) {
        worldGeneratorRegistry.registerStructureGenerator(generator, worldNames, biomes);
    }

    @Override
    public void registerMobSpawner(com.example.modloader.api.mob.CustomMobSpawner spawner) {
        mobRegistry.registerSpawner(spawner);
    }

    @Override
    public com.example.modloader.CustomMobRegistry getCustomMobRegistry() {
        return mobRegistry;
    }

    @Override
    public com.example.modloader.CustomCommandRegistry getCustomCommandRegistry() {
        return commandRegistry;
    }

    @Override
    public CustomInventoryAPI getCustomInventoryAPI() {
        return customInventoryAPI;
    }

    @Override
    public CustomParticleAPI getCustomParticleAPI() {
        return customParticleAPI;
    }

    @Override
    public CustomSoundAPI getCustomSoundAPI() {
        return customSoundAPI;
    }

    @Override
    public CustomEnchantmentAPI getCustomEnchantmentAPI() {
        return customEnchantmentAPI;
    }

    @Override
    public CustomPotionEffectAPI getCustomPotionEffectAPI() {
        return customPotionEffectAPI;
    }

    @Override
    public CustomWorldGeneratorAPI getCustomWorldGeneratorAPI() {
        return customWorldGeneratorAPI;
    }

    @Override
    public DimensionAPI getDimensionAPI() {
        return dimensionAPI;
    }

    @Override
    public CustomStructureAPI getCustomStructureAPI() {
        return customStructureAPI;
    }
}
