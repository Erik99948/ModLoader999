package com.example.modloader.api.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import java.util.*;
import java.util.function.Consumer;

public interface GUIAPI {
    GUIBuilder createGUI(String title, int rows);
    void openGUI(Player player, GUIBuilder gui);
    void closeGUI(Player player);
    void refreshGUI(Player player);
    
    interface GUIBuilder {
        GUIBuilder setItem(int slot, ItemStack item);
        GUIBuilder setItem(int slot, ItemStack item, Consumer<InventoryClickEvent> onClick);
        GUIBuilder setItem(int row, int column, ItemStack item);
        GUIBuilder setItem(int row, int column, ItemStack item, Consumer<InventoryClickEvent> onClick);
        GUIBuilder fillBorder(ItemStack item);
        GUIBuilder fill(ItemStack item);
        GUIBuilder onClose(Consumer<Player> onClose);
        GUIBuilder onOpen(Consumer<Player> onOpen);
        Inventory build();
        String getTitle();
        int getSize();
    }
}

class GUIAPIImpl implements GUIAPI {
    private final GUISystem guiSystem;

    public GUIAPIImpl(GUISystem guiSystem) {
        this.guiSystem = guiSystem;
    }

    @Override
    public GUIBuilder createGUI(String title, int rows) {
        return new GUIBuilderImpl(title, rows);
    }

    @Override
    public void openGUI(Player player, GUIBuilder gui) {
        if (gui instanceof GUIBuilderImpl) {
            GUIBuilderImpl impl = (GUIBuilderImpl) gui;
            GUISystem.GUIInstance instance = guiSystem.createGUI(gui.getTitle(), gui.getSize());
            impl.items.forEach((slot, item) -> instance.setItem(slot, item));
            impl.handlers.forEach((slot, handler) -> instance.setItem(slot, impl.items.get(slot), handler));
            if (impl.onClose != null) instance.onClose(impl.onClose);
            if (impl.onOpen != null) instance.onOpen(impl.onOpen);
            guiSystem.openGUI(player, instance);
        }
    }

    @Override
    public void closeGUI(Player player) {
        player.closeInventory();
    }

    @Override
    public void refreshGUI(Player player) {
        player.updateInventory();
    }

    private static class GUIBuilderImpl implements GUIBuilder {
        private final String title;
        private final int size;
        final Map<Integer, ItemStack> items = new HashMap<>();
        final Map<Integer, Consumer<InventoryClickEvent>> handlers = new HashMap<>();
        Consumer<Player> onClose;
        Consumer<Player> onOpen;

        public GUIBuilderImpl(String title, int rows) {
            this.title = title;
            this.size = rows * 9;
        }

        @Override
        public GUIBuilder setItem(int slot, ItemStack item) {
            items.put(slot, item);
            return this;
        }

        @Override
        public GUIBuilder setItem(int slot, ItemStack item, Consumer<InventoryClickEvent> onClick) {
            items.put(slot, item);
            if (onClick != null) handlers.put(slot, onClick);
            return this;
        }

        @Override
        public GUIBuilder setItem(int row, int column, ItemStack item) {
            return setItem(row * 9 + column, item);
        }

        @Override
        public GUIBuilder setItem(int row, int column, ItemStack item, Consumer<InventoryClickEvent> onClick) {
            return setItem(row * 9 + column, item, onClick);
        }

        @Override
        public GUIBuilder fillBorder(ItemStack item) {
            for (int i = 0; i < 9; i++) items.put(i, item);
            for (int i = size - 9; i < size; i++) items.put(i, item);
            for (int i = 9; i < size - 9; i += 9) {
                items.put(i, item);
                items.put(i + 8, item);
            }
            return this;
        }

        @Override
        public GUIBuilder fill(ItemStack item) {
            for (int i = 0; i < size; i++) items.put(i, item);
            return this;
        }

        @Override
        public GUIBuilder onClose(Consumer<Player> onClose) {
            this.onClose = onClose;
            return this;
        }

        @Override
        public GUIBuilder onOpen(Consumer<Player> onOpen) {
            this.onOpen = onOpen;
            return this;
        }

        @Override
        @SuppressWarnings("deprecation")
        public Inventory build() {
            Inventory inv = Bukkit.createInventory(null, size, title);
            items.forEach(inv::setItem);
            return inv;
        }

        @Override
        public String getTitle() { return title; }

        @Override
        public int getSize() { return size; }
    }
}
