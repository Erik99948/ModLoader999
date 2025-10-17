package com.example.modloader.api;

import com.example.modloader.CustomItemRegistry;
import com.example.modloader.CustomMobRegistry;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

/**
 * Concrete implementation of the ModAPI interface.
 * This class provides the actual logic for registering items and mobs.
 */
public class ModAPIImpl implements ModAPI {

    private final CustomItemRegistry itemRegistry;
    private final CustomMobRegistry mobRegistry;

    public ModAPIImpl(CustomItemRegistry itemRegistry, CustomMobRegistry mobRegistry) {
        this.itemRegistry = itemRegistry;
        this.mobRegistry = mobRegistry;
    }

    @Override
    public void registerItem(String itemId, ItemStack item) {
        itemRegistry.register(itemId, item);
    }

    @Override
    public void registerMob(String mobId, EntityType baseType) {
        mobRegistry.register(mobId, baseType);
    }
}
