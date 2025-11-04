package com.example.modloader.api.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class SimpleButton implements Component {
    private ItemStack itemStack;
    private final Consumer<Player> action;

    public SimpleButton(ItemStack itemStack, Consumer<Player> action) {
        this.itemStack = itemStack;
        this.action = action;
    }

    @Override
    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if (action != null && event.getWhoClicked() instanceof Player) {
            action.accept((Player) event.getWhoClicked());
        }
    }
}