package com.example.modloader;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.logging.Logger;

public class CustomItemRegistry {

    private final Logger logger;

    public CustomItemRegistry(Plugin plugin) {
        this.logger = plugin.getLogger();
    }

    public void register(String itemId, ItemStack item) {
        logger.info("Registering custom item: " + itemId);
    }
}
