package com.example.modloader.api.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.*;
import java.util.function.Consumer;

public class GUISystem implements Listener {
    private final JavaPlugin plugin;
    private final Map<Inventory, GUIInstance> openGUIs = new HashMap<>();

    public GUISystem(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public GUIInstance createGUI(String title, int size) {
        return new GUIInstance(title, size);
    }

    public void openGUI(Player player, GUIInstance gui) {
        Inventory inv = gui.build();
        openGUIs.put(inv, gui);
        player.openInventory(inv);
        if (gui.onOpen != null) {
            gui.onOpen.accept(player);
        }
    }

    public void closeGUI(Player player) {
        player.closeInventory();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        GUIInstance gui = openGUIs.get(event.getInventory());
        if (gui != null) {
            event.setCancelled(true);
            Consumer<InventoryClickEvent> handler = gui.clickHandlers.get(event.getSlot());
            if (handler != null) {
                handler.accept(event);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        GUIInstance gui = openGUIs.remove(event.getInventory());
        if (gui != null && gui.onClose != null && event.getPlayer() instanceof Player) {
            gui.onClose.accept((Player) event.getPlayer());
        }
    }

    public static class GUIInstance {
        private final String title;
        private final int size;
        private final Map<Integer, ItemStack> items = new HashMap<>();
        final Map<Integer, Consumer<InventoryClickEvent>> clickHandlers = new HashMap<>();
        Consumer<Player> onClose;
        Consumer<Player> onOpen;

        public GUIInstance(String title, int size) {
            this.title = title;
            this.size = size;
        }

        public GUIInstance setItem(int slot, ItemStack item) {
            items.put(slot, item);
            return this;
        }

        public GUIInstance setItem(int slot, ItemStack item, Consumer<InventoryClickEvent> onClick) {
            items.put(slot, item);
            if (onClick != null) {
                clickHandlers.put(slot, onClick);
            }
            return this;
        }

        public GUIInstance setItem(int row, int column, ItemStack item) {
            return setItem(row * 9 + column, item);
        }

        public GUIInstance setItem(int row, int column, ItemStack item, Consumer<InventoryClickEvent> onClick) {
            return setItem(row * 9 + column, item, onClick);
        }

        public GUIInstance fillBorder(ItemStack item) {
            for (int i = 0; i < 9; i++) items.put(i, item);
            for (int i = size - 9; i < size; i++) items.put(i, item);
            for (int i = 9; i < size - 9; i += 9) {
                items.put(i, item);
                items.put(i + 8, item);
            }
            return this;
        }

        public GUIInstance fill(ItemStack item) {
            for (int i = 0; i < size; i++) items.put(i, item);
            return this;
        }

        public GUIInstance onClose(Consumer<Player> onClose) {
            this.onClose = onClose;
            return this;
        }

        public GUIInstance onOpen(Consumer<Player> onOpen) {
            this.onOpen = onOpen;
            return this;
        }

        @SuppressWarnings("deprecation")
        public Inventory build() {
            Inventory inv = Bukkit.createInventory(null, size, title);
            items.forEach(inv::setItem);
            return inv;
        }

        public String getTitle() { return title; }
        public int getSize() { return size; }
    }
}
