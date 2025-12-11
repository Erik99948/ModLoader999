package com.example.modloader.gui;

import com.example.modloader.ModInfo;
import com.example.modloader.ModLoaderService;
import com.example.modloader.api.gui.Button;
import com.example.modloader.api.gui.Component;
import com.example.modloader.api.gui.GUI;
import com.example.modloader.api.gui.GridLayout;
import com.example.modloader.api.gui.Label;
import com.example.modloader.api.gui.SimpleButton;
import org.bukkit.ChatColor;
import com.example.modloader.ModState;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ModManagementGUI extends GUI {

    private final JavaPlugin plugin;
    private final ModLoaderService modLoaderService;
    private final Map<String, Component> modStatusLabels = new ConcurrentHashMap<>();
    private final Map<String, Component> modActionButtons = new ConcurrentHashMap<>();

    public ModManagementGUI(JavaPlugin plugin, ModLoaderService modLoaderService) {
        super("Mod Management", 54, new GridLayout(9, 6));
        this.plugin = plugin;
        this.modLoaderService = modLoaderService;
        initializeGUI();
    }

    private void initializeGUI() {
        int slot = 0;
        for (ModInfo modInfo : modLoaderService.getLoadedModsInfo()) {
            addComponent(new Label(createModInfoItem(modInfo)), new GridLayout.GridConstraints(slot, 0));
            modStatusLabels.put(modInfo.getId(), new Label(createModStatusItem(modInfo)));
            addComponent(modStatusLabels.get(modInfo.getId()), new GridLayout.GridConstraints(slot, 1));

            SimpleButton toggleButton = new SimpleButton(createToggleButtonItem(modInfo), player -> {
                try {
                    if (modInfo.getState() == ModState.ENABLED) {
                        modLoaderService.disableMod(modInfo.getName());
                        player.sendMessage(ChatColor.RED + "Mod '" + modInfo.getName() + "' disabled.");
                    } else {
                        modLoaderService.enableMod(modInfo.getName());
                        player.sendMessage(ChatColor.GREEN + "Mod '" + modInfo.getName() + "' enabled.");
                    }
                    updateModStatus(modInfo);
                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + "Error toggling mod '" + modInfo.getName() + "': " + e.getMessage());
                    plugin.getLogger().severe("Error toggling mod '" + modInfo.getName() + "': " + e.getMessage());
                }
            });
            modActionButtons.put(modInfo.getId(), toggleButton);
            addComponent(toggleButton, new GridLayout.GridConstraints(slot, 2));

            slot++;
        }
    }

    private ItemStack createModInfoItem(ModInfo modInfo) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + modInfo.getName() + " v" + modInfo.getVersion());
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Author: " + modInfo.getAuthor());
        lore.add(ChatColor.GRAY + "Description: " + modInfo.getDescription());
        lore.add(ChatColor.GRAY + "Status: " + modInfo.getState().name());
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createModStatusItem(ModInfo modInfo) {
        Material statusMaterial = modInfo.getState() == ModState.ENABLED ? Material.LIME_DYE : Material.GRAY_DYE;
        String statusName = modInfo.getState() == ModState.ENABLED ? ChatColor.GREEN + "ENABLED" : ChatColor.RED + "DISABLED";

        ItemStack item = new ItemStack(statusMaterial);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(statusName);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createToggleButtonItem(ModInfo modInfo) {
        Material buttonMaterial = modInfo.getState() == ModState.ENABLED ? Material.REDSTONE_BLOCK : Material.EMERALD_BLOCK;
        String buttonName = modInfo.getState() == ModState.ENABLED ? ChatColor.RED + "Disable" : ChatColor.GREEN + "Enable";

        ItemStack item = new ItemStack(buttonMaterial);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(buttonName);
        item.setItemMeta(meta);
        return item;
    }

    private void updateModStatus(ModInfo modInfo) {
        Label statusLabel = (Label) modStatusLabels.get(modInfo.getId());
        if (statusLabel != null) {
            statusLabel.setItemStack(createModStatusItem(modInfo));
        }

        SimpleButton toggleButton = (SimpleButton) modActionButtons.get(modInfo.getId());
        if (toggleButton != null) {
            toggleButton.setItemStack(createToggleButtonItem(modInfo));
        }

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.getOpenInventory().getTopInventory().equals(getInventory())) {
                open(player);
            }
        }
    }
}
