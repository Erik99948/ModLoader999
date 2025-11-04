package com.example.modloader.api.gui;

import org.bukkit.inventory.ItemStack;

public class Label implements Component {
    private ItemStack itemStack;

    public Label(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @Override
    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @Override
    public void handleClick(org.bukkit.event.inventory.InventoryClickEvent event) {
        event.setCancelled(true);
    }
}
