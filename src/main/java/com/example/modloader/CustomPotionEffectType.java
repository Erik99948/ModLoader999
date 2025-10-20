package com.example.modloader;

import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class CustomPotionEffectType extends PotionEffectType {

    private final String name;
    private final Color color;
    private final boolean instant;
    private final PotionEffectType conflict;
    private final NamespacedKey key;

    protected CustomPotionEffectType(JavaPlugin plugin, String namespace, String name, Color color, boolean instant, PotionEffectType conflict) { // Call the deprecated constructor with a dummy string
 super();
        this.key = new NamespacedKey(plugin, namespace);
        this.name = name;
        this.color = color;
        this.instant = instant;
        this.conflict = conflict;

        // Use reflection to set the key field, as the super constructor is deprecated
        try {
            Field keyField = PotionEffectType.class.getDeclaredField("key");
            keyField.setAccessible(true);
            keyField.set(this, this.key);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Logger.getLogger(CustomPotionEffectType.class.getName()).log(Level.SEVERE, "Failed to set NamespacedKey for custom potion effect type: " + name, e);
        }
    }

    @Override
    public NamespacedKey getKey() {
        return key;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isInstant() {
        return instant;
    }

    @Override
    public Color getColor() {
        return color;
    }

    // Bukkit's PotionEffectType does not have getConflictWith() or isSupported() in all versions
    // Mod developers will need to handle conflicts and support logic in their implementation.
}