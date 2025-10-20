package com.example.modloader.api;

import com.example.modloader.CustomPotionEffectType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class CustomPotionEffectAPIImpl implements CustomPotionEffectAPI {

    private final JavaPlugin plugin;
    private final Map<String, CustomPotionEffectType> registeredPotionEffectTypes = new HashMap<>();

    public CustomPotionEffectAPIImpl(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean registerPotionEffectType(CustomPotionEffectType effectType) {
        if (registeredPotionEffectTypes.containsKey(effectType.getKey().getKey())) {
            plugin.getLogger().warning("Attempted to register potion effect type with duplicate key: " + effectType.getKey().getKey());
            return false;
        }
        if (PotionEffectType.getByKey(effectType.getKey()) != null) {
            plugin.getLogger().warning("Attempted to register potion effect type with key that conflicts with a vanilla effect: " + effectType.getKey().getKey());
            return false;
        }

        try {
            Field keyField = PotionEffectType.class.getDeclaredField("byKey");
            keyField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<org.bukkit.NamespacedKey, PotionEffectType> byKey = (Map<org.bukkit.NamespacedKey, PotionEffectType>) keyField.get(null);
            byKey.put(effectType.getKey(), effectType);

            Field nameField = PotionEffectType.class.getDeclaredField("byName");
            nameField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, PotionEffectType> byName = (Map<String, PotionEffectType>) nameField.get(null);
            byName.put(effectType.getName(), effectType);

            registeredPotionEffectTypes.put(effectType.getKey().getKey(), effectType);
            plugin.getLogger().info("Registered custom potion effect type: " + effectType.getName() + " (" + effectType.getKey().getKey() + ")");

            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to register custom potion effect type: " + effectType.getName(), e);
            return false;
        }
    }

    @Override
    public CustomPotionEffectType getPotionEffectType(String namespace) {
        return registeredPotionEffectTypes.get(namespace);
    }

    @Override
    public boolean applyPotionEffect(LivingEntity entity, CustomPotionEffectType effectType, int duration, int amplifier, boolean ambient, boolean particles, boolean icon) {
        if (effectType == null) {
            plugin.getLogger().warning("Attempted to apply a null custom potion effect type.");
            return false;
        }
        PotionEffect potionEffect = new PotionEffect(effectType, duration, amplifier, ambient, particles, icon);
        return entity.addPotionEffect(potionEffect);
    }

    @Override
    public boolean applyPotionEffect(LivingEntity entity, PotionEffect effect) {
        if (effect == null) {
            plugin.getLogger().warning("Attempted to apply a null potion effect.");
            return false;
        }
        return entity.addPotionEffect(effect);
    }

    // Method to unregister all custom potion effect types (useful on plugin disable)
    public void unregisterAll() {
        try {
            Field byKeyField = PotionEffectType.class.getDeclaredField("byKey");
            byKeyField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<org.bukkit.NamespacedKey, PotionEffectType> byKey = (Map<org.bukkit.NamespacedKey, PotionEffectType>) byKeyField.get(null);

            Field byNameField = PotionEffectType.class.getDeclaredField("byName");
            byNameField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, PotionEffectType> byName = (Map<String, PotionEffectType>) byNameField.get(null);

            for (CustomPotionEffectType effectType : registeredPotionEffectTypes.values()) {
                byKey.remove(effectType.getKey());
                byName.remove(effectType.getName());
                plugin.getLogger().info("Unregistered custom potion effect type: " + effectType.getName());
            }
            registeredPotionEffectTypes.clear();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to unregister custom potion effect types.", e);
        }
    }
}