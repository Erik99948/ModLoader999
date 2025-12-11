package com.example.modloader.api;

import com.example.modloader.CustomEnchantment;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class CustomEnchantmentAPIImpl implements CustomEnchantmentAPI {

    private final JavaPlugin plugin;
    private final Map<String, CustomEnchantment> registeredEnchantments = new HashMap<>();

    public CustomEnchantmentAPIImpl(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean registerEnchantment(CustomEnchantment enchantment) {
        if (registeredEnchantments.containsKey(enchantment.getKey().getKey())) {
            plugin.getLogger().warning("Attempted to register enchantment with duplicate key: " + enchantment.getKey().getKey());
            return false;
        }
        if (Enchantment.getByKey(enchantment.getKey()) != null) {
            plugin.getLogger().warning("Attempted to register enchantment with key that conflicts with a vanilla enchantment: " + enchantment.getKey().getKey());
            return false;
        }

        try {
            Field keyField = Enchantment.class.getDeclaredField("byKey");
            keyField.setAccessible(true);
            Map<org.bukkit.NamespacedKey, Enchantment> byKey = (Map<org.bukkit.NamespacedKey, Enchantment>) keyField.get(null);
            byKey.put(enchantment.getKey(), enchantment);

            Field nameField = Enchantment.class.getDeclaredField("byName");
            nameField.setAccessible(true);
            Map<String, Enchantment> byName = (Map<String, Enchantment>) nameField.get(null);
            byName.put(enchantment.getName(), enchantment);

            registeredEnchantments.put(enchantment.getKey().getKey(), enchantment);
            plugin.getLogger().info("Registered custom enchantment: " + enchantment.getName() + " (" + enchantment.getKey().getKey() + ")");

            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to register custom enchantment: " + enchantment.getName(), e);
            return false;
        }
    }

    @Override
    public CustomEnchantment getEnchantment(String namespace) {
        return registeredEnchantments.get(namespace);
    }

    @Override
    public CustomEnchantment getEnchantmentByName(String name) {
        for (CustomEnchantment enchantment : registeredEnchantments.values()) {
            if (enchantment.getName().equalsIgnoreCase(name)) {
                return enchantment;
            }
        }
        return null;
    }

    public void unregisterAll() {
        try {
            Field keyField = Enchantment.class.getDeclaredField("byKey");
            keyField.setAccessible(true);
            Map<org.bukkit.NamespacedKey, Enchantment> byKey = (Map<org.bukkit.NamespacedKey, Enchantment>) keyField.get(null);

            Field nameField = Enchantment.class.getDeclaredField("byName");
            nameField.setAccessible(true);
            Map<String, Enchantment> byName = (Map<String, Enchantment>) nameField.get(null);

            for (CustomEnchantment enchantment : registeredEnchantments.values()) {
                byKey.remove(enchantment.getKey());
                byName.remove(enchantment.getName());
                plugin.getLogger().info("Unregistered custom enchantment: " + enchantment.getName());
            }
            registeredEnchantments.clear();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to unregister custom enchantments.", e);
        }
    }
}
