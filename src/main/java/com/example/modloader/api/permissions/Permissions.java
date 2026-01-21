package com.example.modloader.api.permissions;

import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.*;

/**
 * Permission management system for mods.
 */
public class Permissions {
    private final JavaPlugin plugin;
    private final Map<String, Permission> registeredPermissions = new HashMap<>();
    private final Map<UUID, Set<String>> playerPermissions = new HashMap<>();

    public Permissions(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerPermission(String permission, String description, PermissionDefault defaultValue) {
        Permission perm = new Permission(permission, description, defaultValue);
        plugin.getServer().getPluginManager().addPermission(perm);
        registeredPermissions.put(permission, perm);
    }

    public void registerPermission(String permission, String description) {
        registerPermission(permission, description, PermissionDefault.OP);
    }

    public boolean hasPermission(Player player, String permission) {
        return player.hasPermission(permission);
    }

    public void grantPermission(Player player, String permission) {
        player.addAttachment(plugin, permission, true);
        playerPermissions.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>()).add(permission);
    }

    public void revokePermission(Player player, String permission) {
        player.addAttachment(plugin, permission, false);
        Set<String> perms = playerPermissions.get(player.getUniqueId());
        if (perms != null) {
            perms.remove(permission);
        }
    }

    public Set<String> getPlayerPermissions(Player player) {
        return playerPermissions.getOrDefault(player.getUniqueId(), Collections.emptySet());
    }

    public boolean isPermissionRegistered(String permission) {
        return registeredPermissions.containsKey(permission);
    }

    public void unregisterPermission(String permission) {
        Permission perm = registeredPermissions.remove(permission);
        if (perm != null) {
            plugin.getServer().getPluginManager().removePermission(perm);
        }
    }

    public void unregisterAll() {
        for (Permission perm : registeredPermissions.values()) {
            plugin.getServer().getPluginManager().removePermission(perm);
        }
        registeredPermissions.clear();
        playerPermissions.clear();
    }
}
