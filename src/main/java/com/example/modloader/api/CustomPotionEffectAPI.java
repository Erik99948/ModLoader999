package com.example.modloader.api;

import com.example.modloader.CustomPotionEffectType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;

public interface CustomPotionEffectAPI {

    /**
     * Registers a custom potion effect type with the server.
     *
     * @param effectType The custom potion effect type to register.
     * @return true if the effect type was registered successfully, false otherwise.
     */
    boolean registerPotionEffectType(CustomPotionEffectType effectType);

    /**
     * Gets a registered custom potion effect type by its key (namespace).
     *
     * @param namespace The namespace (key) of the potion effect type.
     * @return The CustomPotionEffectType if found, null otherwise.
     */
    CustomPotionEffectType getPotionEffectType(String namespace);

    /**
     * Applies a custom potion effect to a living entity.
     *
     * @param entity The living entity to apply the effect to.
     * @param effectType The custom potion effect type.
     * @param duration The duration of the effect in ticks.
     * @param amplifier The amplifier of the effect (0 for level I, 1 for level II, etc.).
     * @param ambient Whether the effect is ambient (no particles).
     * @param particles Whether the effect shows particles.
     * @param icon Whether the effect shows an icon.
     * @return true if the effect was applied successfully, false otherwise.
     */
    boolean applyPotionEffect(LivingEntity entity, CustomPotionEffectType effectType, int duration, int amplifier, boolean ambient, boolean particles, boolean icon);

    /**
     * Applies a custom potion effect to a living entity.
     *
     * @param entity The living entity to apply the effect to.
     * @param effect The PotionEffect object representing the custom effect.
     * @return true if the effect was applied successfully, false otherwise.
     */
    boolean applyPotionEffect(LivingEntity entity, PotionEffect effect);
}