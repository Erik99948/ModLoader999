package com.example.modloader.api.gui;

import org.bukkit.inventory.ItemStack;

public interface Component {
    ItemStack getItemStack();
    void handleClick(org.bukkit.event.inventory.InventoryClickEvent event);
}

