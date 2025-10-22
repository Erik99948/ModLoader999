package com.example.modloader;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class CustomEnchantment extends Enchantment {

    private final String name;
    private final int maxLevel;
    private final EnchantmentTarget itemTarget;
    private final boolean treasure;
    private final boolean cursed;
    private final NamespacedKey key;

    protected CustomEnchantment(JavaPlugin plugin, String namespace, String name, int maxLevel, EnchantmentTarget itemTarget, boolean treasure, boolean cursed) {
        this.key = new NamespacedKey(plugin, namespace);
        this.name = name;
        this.maxLevel = maxLevel;
        this.itemTarget = itemTarget;
        this.treasure = treasure;
        this.cursed = cursed;
    }

    @Override
    public NamespacedKey getKey() {
        return key;
    }

    public boolean isSupported() {
        return true;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getMaxLevel() {
        return maxLevel;
    }

    @Override
    public int getStartLevel() {
        return 1;
    }

    @Override
    public EnchantmentTarget getItemTarget() {
        return itemTarget;
    }

    @Override
    public boolean isTreasure() {
        return treasure;
    }

    @Override
    public boolean isCursed() {
        return cursed;
    }

    @Override
    public boolean conflictsWith(Enchantment other) {
        return false;
    }

    @Override
    public boolean canEnchantItem(ItemStack item) {
        return itemTarget.includes(item);
    }
}