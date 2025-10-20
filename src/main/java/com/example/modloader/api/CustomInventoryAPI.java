package com.example.modloader.api;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public interface CustomInventoryAPI {

    /**
     * Creates a new custom inventory.
     *
     * @param size The size of the inventory (must be a multiple of 9, up to 54).
     * @param title The title of the inventory.
     * @return A new custom Inventory instance.
     */
    Inventory createInventory(int size, String title);

    /**
     * Opens a custom inventory for a player.
     *
     * @param player The player to open the inventory for.
     * @param inventory The custom inventory to open.
     */
    void openInventory(Player player, Inventory inventory);

    /**
     * Registers a click handler for a specific custom inventory.
     * The handler will be called when a player clicks a slot in the registered inventory.
     *
     * @param inventory The custom inventory to register the handler for.
     * @param clickHandler The consumer that handles the InventoryClickEvent.
     */
    void registerClickHandler(Inventory inventory, Consumer<InventoryClickEvent> clickHandler);

    /**
     * Registers a close handler for a specific custom inventory.
     * The handler will be called when a player closes the registered inventory.
     *
     * @param inventory The custom inventory to register the handler for.
     * @param closeHandler The consumer that handles the InventoryCloseEvent.
     */
    void registerCloseHandler(Inventory inventory, Consumer<InventoryCloseEvent> closeHandler);

    /**
     * Sets an item in a specific slot of a custom inventory.
     *
     * @param inventory The custom inventory.
     * @param slot The slot index.
     * @param item The ItemStack to set.
     */
    void setItem(Inventory inventory, int slot, ItemStack item);

    /**
     * Gets the item from a specific slot of a custom inventory.
     *
     * @param inventory The custom inventory.
     * @param slot The slot index.
     * @return The ItemStack in the specified slot, or null if empty.
     */
    ItemStack getItem(Inventory inventory, int slot);
}