package com.example.modloader.api;

import com.example.modloader.CustomPotionEffectType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;

public interface CustomPotionEffectAPI {

    boolean registerPotionEffectType(CustomPotionEffectType effectType);

    CustomPotionEffectType getPotionEffectType(String namespace);

    boolean applyPotionEffect(LivingEntity entity, CustomPotionEffectType effectType, int duration, int amplifier, boolean ambient, boolean particles, boolean icon);

    boolean applyPotionEffect(LivingEntity entity, PotionEffect effect);
}