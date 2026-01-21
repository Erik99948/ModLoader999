package com.example.modloader.api;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import java.util.function.Consumer;

/**
 * API for creating and managing custom inventories/GUIs.
 */
public interface CustomInventoryAPI {
    Inventory createInventory(int size, String title);
    void openInventory(Player player, Inventory inventory);
    void registerClickHandler(Inventory inventory, Consumer<InventoryClickEvent> clickHandler);
    void registerCloseHandler(Inventory inventory, Consumer<InventoryCloseEvent> closeHandler);
    void setItem(Inventory inventory, int slot, ItemStack item);
    ItemStack getItem(Inventory inventory, int slot);
}
