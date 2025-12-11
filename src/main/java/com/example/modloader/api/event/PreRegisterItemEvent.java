package com.example.modloader.api.event;

import org.bukkit.inventory.ItemStack;

public class PreRegisterItemEvent extends CancellableModEvent {
    private final String itemId;
    private final ItemStack item;

    public PreRegisterItemEvent(String itemId, ItemStack item) {
        this.itemId = itemId;
        this.item = item;
    }

    public String getItemId() {
        return itemId;
    }

    public ItemStack getItem() {
        return item;
    }
}

