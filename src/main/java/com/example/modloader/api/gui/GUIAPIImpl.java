package com.example.modloader.api.gui;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class GUIAPIImpl implements GUIAPI {

    public GUIAPIImpl(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(new GUIListener(this), plugin);
    }

    private final java.util.Map<org.bukkit.inventory.Inventory, GUI> openGUIs = new java.util.HashMap<>();

    @Override
    public GUI createGUI(String title, int size, Layout layout) {
        return new GUI(title, size, layout);
    }

    @Override
    public void openGUI(Player player, GUI gui) {
        gui.open(player);
        openGUIs.put(gui.getInventory(), gui);
    }

    public GUI getGUI(org.bukkit.inventory.Inventory inventory) {
        return openGUIs.get(inventory);
    }
}

