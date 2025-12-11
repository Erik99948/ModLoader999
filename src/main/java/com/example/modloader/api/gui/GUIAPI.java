package com.example.modloader.api.gui;

import org.bukkit.entity.Player;

public interface GUIAPI {
    GUI createGUI(String title, int size, Layout layout);
    void openGUI(Player player, GUI gui);
}

