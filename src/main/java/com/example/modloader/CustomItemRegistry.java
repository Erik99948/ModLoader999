package com.example.modloader;

import com.example.modloader.api.event.EventBus;
import com.example.modloader.api.event.PreRegisterItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class CustomItemRegistry {

    private final Logger logger;
    private final Map<String, ItemStack> registeredItems = new HashMap<>();
    private final EventBus eventBus;

    public CustomItemRegistry(Plugin plugin, EventBus eventBus) {
        this.logger = plugin.getLogger();
        this.eventBus = eventBus;
    }

    public void register(String itemId, ItemStack item) {
        PreRegisterItemEvent event = new PreRegisterItemEvent(itemId, item);
        eventBus.post(event);
        if (event.isCancelled()) {
            logger.warning("Custom item registration for '" + itemId + "' was cancelled by another mod. Skipping.");
            return;
        }

        if (registeredItems.containsKey(itemId)) {
            logger.warning("Custom item with ID '" + itemId + "' already registered. Skipping.");
            return;
        }
        registeredItems.put(itemId, item);
        logger.info("Registered custom item: " + itemId);
    }

    public ItemStack getItemStack(String id) {
        return registeredItems.get(id);
    }
}

