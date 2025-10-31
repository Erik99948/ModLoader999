package com.example.examplemod;

import com.example.modloader.api.ModAPI;
import com.example.modloader.api.ModInitializer;
import com.example.modloader.CustomBlock;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

public class ExampleMod implements ModInitializer {

    private ModAPI modAPI;

    @Override
    public void onPreLoad(ModAPI modAPI) {
        this.modAPI = modAPI;
        JavaPlugin plugin = (JavaPlugin) modAPI.getEventBus().getPlugin();

        ItemStack darkIngot = DarkIngot.getItemStack();
        modAPI.registerItem("dark_ingot", darkIngot);
        modAPI.getCustomAssetAPI().registerTexture("dark_ingot_texture", "stuff/darkingot.png");
        modAPI.getCustomAssetAPI().registerModel("dark_ingot_model", "stuff/darkingotmodel.json");

        CustomBlock darkOreBlock = new DarkOreBlock(plugin);
        modAPI.registerBlock(darkOreBlock);
        modAPI.getCustomAssetAPI().registerTexture("dark_ore_texture", "stuff/darkore.png");
        modAPI.getCustomAssetAPI().registerModel("dark_ore_block_model", "stuff/darkoreblockmodel.json");

        NamespacedKey smeltingKey = new NamespacedKey(plugin, "dark_ore_smelting");
        modAPI.registerRecipe(new org.bukkit.inventory.FurnaceRecipe(smeltingKey, darkIngot, darkOreBlock.getMaterial(), 0.7F, 200));
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