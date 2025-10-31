package com.example.examplemod;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

public class DarkIngot {

    public static ItemStack getItemStack() {
        ItemStack item = new ItemStack(Material.IRON_INGOT);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§5Dark Ingot");
            meta.setLore(Collections.singletonList("§7A mysterious dark ingot."));
            item.setItemMeta(meta);
        }
        return item;
    }
}