package com.example.modloader;

import com.example.modloader.api.block.BlockBreakBehavior;
import com.example.modloader.api.block.BlockExplodeBehavior;
import com.example.modloader.api.block.BlockInteractBehavior;
import com.example.modloader.api.block.BlockPlaceBehavior;
import com.example.modloader.api.block.BlockRedstoneBehavior;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.List;

public class CustomBlock {
    private final String id;
    private final Material baseMaterial;
    private final int customModelData;
    private final String displayName;
    private final List<String> lore;
    private final BlockPlaceBehavior placeBehavior;
    private final BlockBreakBehavior breakBehavior;
    private final BlockInteractBehavior interactBehavior;
    private final BlockRedstoneBehavior redstoneBehavior;
    private final BlockExplodeBehavior explodeBehavior;
    private final List<ItemStack> customDrops;

    public CustomBlock(String id, Material baseMaterial, int customModelData, String displayName, List<String> lore,
                       BlockPlaceBehavior placeBehavior, BlockBreakBehavior breakBehavior, BlockInteractBehavior interactBehavior, BlockRedstoneBehavior redstoneBehavior, BlockExplodeBehavior explodeBehavior, List<ItemStack> customDrops) {
        this.id = id;
        this.baseMaterial = baseMaterial;
        this.customModelData = customModelData;
        this.displayName = displayName;
        this.lore = lore != null ? Collections.unmodifiableList(lore) : Collections.emptyList();
        this.placeBehavior = placeBehavior;
        this.breakBehavior = breakBehavior;
        this.interactBehavior = interactBehavior;
        this.redstoneBehavior = redstoneBehavior;
        this.explodeBehavior = explodeBehavior;
        this.customDrops = customDrops != null ? Collections.unmodifiableList(customDrops) : Collections.emptyList();
    }

    public String getId() {
        return id;
    }

    public Material getBaseMaterial() {
        return baseMaterial;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getLore() {
        return lore;
    }

    public BlockPlaceBehavior getPlaceBehavior() {
        return placeBehavior;
    }

    public BlockBreakBehavior getBreakBehavior() {
        return breakBehavior;
    }

    public BlockInteractBehavior getInteractBehavior() {
        return interactBehavior;
    }

    public BlockRedstoneBehavior getRedstoneBehavior() {
        return redstoneBehavior;
    }

    public BlockExplodeBehavior getExplodeBehavior() {
        return explodeBehavior;
    }

    public List<ItemStack> getCustomDrops() {
        return customDrops;
    }

    public NamespacedKey getNamespacedKey(Plugin plugin) {
        return new NamespacedKey(plugin, id);
    }

    public ItemStack getItemStack() {
        ItemStack item = new ItemStack(baseMaterial);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            meta.setLore(lore);
            meta.setCustomModelData(customModelData);
            item.setItemMeta(meta);
        }
        return item;
    }

    public org.bukkit.persistence.PersistentDataContainer getPersistentDataContainer(org.bukkit.World world, org.bukkit.Location location) {
        org.bukkit.block.Block block = world.getBlockAt(location);
        if (block.getState() instanceof org.bukkit.block.TileState) {
            return ((org.bukkit.block.TileState) block.getState()).getPersistentDataContainer();
        }
        return null;
    }
}

