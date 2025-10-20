package com.example.modloader.api;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class CustomInventoryAPIImpl implements CustomInventoryAPI {

    private final JavaPlugin plugin;
    private final Map<Inventory, Consumer<InventoryClickEvent>> clickHandlers = new HashMap<>();
    private final Map<Inventory, Consumer<InventoryCloseEvent>> closeHandlers = new HashMap<>();

    public CustomInventoryAPIImpl(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Inventory createInventory(int size, String title) {
        if (size % 9 != 0 || size <= 0 || size > 54) {
            throw new IllegalArgumentException("Inventory size must be a multiple of 9 and between 9 and 54.");
        }
        return Bukkit.createInventory(null, size, title);
    }

    @Override
    public void openInventory(Player player, Inventory inventory) {
        player.openInventory(inventory);
    }

    @Override
    public void registerClickHandler(Inventory inventory, Consumer<InventoryClickEvent> clickHandler) {
        clickHandlers.put(inventory, clickHandler);
    }

    @Override
    public void registerCloseHandler(Inventory inventory, Consumer<InventoryCloseEvent> closeHandler) {
        closeHandlers.put(inventory, closeHandler);
    }

    @Override
    public void setItem(Inventory inventory, int slot, ItemStack item) {
        inventory.setItem(slot, item);
    }

    @Override
    public ItemStack getItem(Inventory inventory, int slot) {
        return inventory.getItem(slot);
    }

    // Internal methods for event handling (called by the listener)
    public void handleClick(InventoryClickEvent event) {
        Consumer<InventoryClickEvent> handler = clickHandlers.get(event.getInventory());
        if (handler != null) {
            handler.accept(event);
        }
    }

    public void handleClose(InventoryCloseEvent event) {
        Consumer<InventoryCloseEvent> handler = closeHandlers.get(event.getInventory());
        if (handler != null) {
            handler.accept(event);
        }
    }

    public boolean isCustomInventory(Inventory inventory) {
        return clickHandlers.containsKey(inventory) || closeHandlers.containsKey(inventory);
    }
}