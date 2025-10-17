package com.example.modloader;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.logging.Logger;

/**
 * A service for registering custom items.
 * An instance of this class is provided by the ModLoader engine.
 */
public class CustomItemRegistry {

    private final Logger logger;

    public CustomItemRegistry(Plugin plugin) {
        this.logger = plugin.getLogger();
    }

    /**
     * Registers a custom item with the engine.
     * (Currently, this just logs the registration).
     *
     * @param itemId A unique identifier for your item.
     * @param item The ItemStack representing your custom item.
     */
    public void register(String itemId, ItemStack item) {
        // In the future, this will add the item to a proper registry.
        logger.info("Registering custom item: " + itemId);
    }
}
