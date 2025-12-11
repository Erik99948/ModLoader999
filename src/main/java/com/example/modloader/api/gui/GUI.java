package com.example.modloader.api.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

public class GUI {
    private final String title;
    private final int size;
    private final Layout layout;
    private final Map<Component, Object> componentConstraints = new HashMap<>();
    private Map<Integer, Component> arrangedComponents = new HashMap<>();
    private Inventory inventory;

    public GUI(String title, int size, Layout layout) {
        this.title = title;
        this.size = size;
        this.layout = layout;
    }

    public void addComponent(Component component, Object constraints) {
        componentConstraints.put(component, constraints);
    }

    public void open(Player player) {
        arrangedComponents = layout.arrangeComponents(componentConstraints);
        inventory = Bukkit.createInventory(null, size, title);
        for (Map.Entry<Integer, Component> entry : arrangedComponents.entrySet()) {
            inventory.setItem(entry.getKey(), entry.getValue().getItemStack());
        }
        player.openInventory(inventory);
    }

    public Inventory getInventory() {
        return inventory;
    }

    public Component getComponent(int slot) {
        return arrangedComponents.get(slot);
    }

    public void updateComponent(Component component, Object constraints) {
        componentConstraints.put(component, constraints);
        arrangedComponents = layout.arrangeComponents(componentConstraints);
        if (inventory != null) {
            inventory.clear();
            for (Map.Entry<Integer, Component> entry : arrangedComponents.entrySet()) {
                inventory.setItem(entry.getKey(), entry.getValue().getItemStack());
            }
        }
    }

    public void removeComponent(Component component) {
        componentConstraints.remove(component);
        arrangedComponents = layout.arrangeComponents(componentConstraints);
        if (inventory != null) {
            inventory.clear();
            for (Map.Entry<Integer, Component> entry : arrangedComponents.entrySet()) {
                inventory.setItem(entry.getKey(), entry.getValue().getItemStack());
            }
        }
    }
}

