package com.example.modloader.api;

import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;

/**
 * API for custom potion effects.
 */
public interface CustomPotionEffectAPI {
    boolean registerPotionEffectType(Object effectType);
    Object getPotionEffectType(String namespace);
    boolean applyPotionEffect(LivingEntity entity, Object effectType, int duration, int amplifier, boolean ambient, boolean particles, boolean icon);
    boolean applyPotionEffect(LivingEntity entity, PotionEffect effect);
}
