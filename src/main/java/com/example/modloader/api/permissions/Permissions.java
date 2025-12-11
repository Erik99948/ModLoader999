package com.example.modloader.api.permissions;

import org.bukkit.entity.Player;

public class Permissions {

    public boolean has(Player player, String permission) {
        return player.hasPermission(permission);
    }
}

