package com.example.modloader.api.gui;

import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class Button implements Component {
    private final ItemStack itemStack;
    private final Consumer<org.bukkit.event.inventory.InventoryClickEvent> clickHandler;

    public Button(ItemStack itemStack, Consumer<org.bukkit.event.inventory.InventoryClickEvent> clickHandler) {
        this.itemStack = itemStack;
        this.clickHandler = clickHandler;
    }

    @Override
    public ItemStack getItemStack() {
        return itemStack;
    }

    @Override
    public void handleClick(org.bukkit.event.inventory.InventoryClickEvent event) {
        event.setCancelled(true);
        if (clickHandler != null) {
            clickHandler.accept(event);
        }
    }
}
