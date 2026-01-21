package com.example.modloader.api;

/**
 * API for custom enchantments.
 */
public interface CustomEnchantmentAPI {
    boolean registerEnchantment(Object enchantment);
    Object getEnchantment(String namespace);
    Object getEnchantmentByName(String name);
}
