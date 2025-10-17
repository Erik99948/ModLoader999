package com.example.modloader.api;

import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

/**
 * The API provided to custom mods for registering their content.
 * Mod developers will use this interface to interact with the ModLoader engine.
 */
public interface ModAPI {

    /**
     * Registers a custom item with the ModLoader engine.
     *
     * @param itemId A unique identifier for the item (e.g., "fire_sword").
     * @param item The ItemStack representing the custom item.
     */
    void registerItem(String itemId, ItemStack item);

    /**
     * Registers a custom mob with the ModLoader engine.
     *
     * @param mobId A unique identifier for the mob (e.g., "fire_beast").
     * @param baseType The vanilla EntityType to base this custom mob on.
     */
    void registerMob(String mobId, EntityType baseType);

    // Future methods could include:
    // void registerCommand(String name, CommandExecutor executor);
    // void registerBlock(String blockId, Material baseMaterial);
    // void registerRecipe(Recipe recipe);
}
