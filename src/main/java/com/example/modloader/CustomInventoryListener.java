package com.example.modloader;

import com.example.modloader.api.CustomInventoryAPIImpl;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class CustomInventoryListener implements Listener {

    private final CustomInventoryAPIImpl customInventoryAPI;

    public CustomInventoryListener(CustomInventoryAPIImpl customInventoryAPI, JavaPlugin plugin) {
        this.customInventoryAPI = customInventoryAPI;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (customInventoryAPI.isCustomInventory(event.getInventory())) {
            customInventoryAPI.handleClick(event);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (customInventoryAPI.isCustomInventory(event.getInventory())) {
            customInventoryAPI.handleClose(event);
        }
    }
}
