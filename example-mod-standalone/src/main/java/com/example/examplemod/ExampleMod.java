package com.example.examplemod;

import com.example.modloader.api.ModAPI;
import com.example.modloader.api.ModInitializer;
import com.example.modloader.CustomBlock;
import com.example.modloader.api.dependencyinjection.Binder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import com.example.modloader.CustomOreWorldPopulator;
import org.bukkit.block.data.BlockData;
import org.bukkit.plugin.java.JavaPlugin;

import com.example.modloader.api.config.ModConfigProvider;
import com.example.modloader.api.config.ConfigProperty;
import com.example.modloader.api.config.ModConfig;

public class ExampleMod implements ModInitializer {

    private ModAPI modAPI;
    private ExampleModConfig config;

    @ModConfigProvider
    public static ExampleModConfig provideConfig() {
        return new ExampleModConfig();
    }

    public static class ExampleModConfig implements ModConfig {
        @ConfigProperty(path = "general.enableDarkOre", defaultValue = "true", description = "Enable or disable Dark Ore generation.")
        public boolean enableDarkOre = true;

        @ConfigProperty(path = "general.generationChance", defaultValue = "0.05", minValue = 0.0, maxValue = 1.0, description = "Chance for Dark Ore to generate (0.0-1.0).")
        public double generationChance = 0.05;

        @ConfigProperty(path = "general.maxVeinSize", defaultValue = "8", minValue = 1, maxValue = 64, description = "Maximum size of Dark Ore veins.")
        public int maxVeinSize = 8;

        @ConfigProperty(path = "messages.welcomeMessage", defaultValue = "Welcome to ExampleMod!", description = "Message displayed to players on join.")
        public String welcomeMessage = "Welcome to ExampleMod!";

        @ConfigProperty(path = "messages.adminEmail", defaultValue = "admin@example.com", pattern = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$", description = "Admin email for notifications.")
        public String adminEmail = "admin@example.com";

        @ConfigProperty(path = "difficulty", allowedValues = {"EASY", "NORMAL", "HARD"}, defaultValue = "NORMAL", description = "Mod difficulty setting.")
        public String difficulty = "NORMAL";
    }

    @Override
    public void configure(Binder binder) {

    }

    @Override
    public void onPreLoad(ModAPI modAPI) {
        this.modAPI = modAPI;
        this.config = modAPI.getModConfig("examplemod", ExampleModConfig.class);
        JavaPlugin plugin = modAPI.getPlugin();

        if (config.enableDarkOre) {
            ItemStack darkIngot = DarkIngot.getItemStack();
            modAPI.registerItem("dark_ingot", darkIngot);
            modAPI.getCustomAssetAPI().registerTexture("dark_ingot_texture", "stuff/darkingot.png");
            modAPI.getCustomAssetAPI().registerModel("dark_ingot_model", "stuff/darkingotmodel.json");

            CustomBlock darkOreBlock = new DarkOreBlock();
            modAPI.registerBlock(darkOreBlock);
            modAPI.getCustomAssetAPI().registerTexture("dark_ore_texture", "stuff/darkore.png");
            modAPI.getCustomAssetAPI().registerModel("dark_ore_block_model", "stuff/darkoreblockmodel.json");

            NamespacedKey smeltingKey = new NamespacedKey(plugin, "dark_ore_smelting");
            modAPI.registerRecipe(new org.bukkit.inventory.FurnaceRecipe(smeltingKey, darkIngot, darkOreBlock.getBaseMaterial(), 0.7F, 200));


            BlockData darkOreBlockData = darkOreBlock.getBaseMaterial().createBlockData();
            modAPI.registerWorldPopulator(new CustomOreWorldPopulator(darkOreBlockData, 0, 60, config.generationChance, 1, config.maxVeinSize), new String[]{"world"});
        }

        plugin.getLogger().info(config.welcomeMessage);
        plugin.getLogger().info("Admin Email: " + config.adminEmail);
        plugin.getLogger().info("Difficulty: " + config.difficulty);

        modAPI.registerEventListener(new ExampleModListener(modAPI.getPlugin()));
    }

    @Override
    public void onLoad(ModAPI modAPI) {
    }

    @Override
    public void onPostLoad(ModAPI modAPI) {
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }

    @Override
    public void onPreDisable() {
    }

    @Override
    public void onPostDisable() {
    }
}