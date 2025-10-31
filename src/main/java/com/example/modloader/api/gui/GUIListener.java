package com.example.modloader.api.gui;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GUIListener implements Listener {
    private final GUIAPIImpl guiApi;

    public GUIListener(GUIAPIImpl guiApi) {
        this.guiApi = guiApi;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        GUI gui = guiApi.getGUI(event.getInventory());
        if (gui != null) {
            Component component = gui.getComponent(event.getSlot());
            if (component != null) {
                component.handleClick(event);
            }
        }
    }
}
