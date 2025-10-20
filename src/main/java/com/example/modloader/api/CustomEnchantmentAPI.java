package com.example.modloader.api;

import com.example.modloader.CustomEnchantment;
import org.bukkit.enchantments.Enchantment;

public interface CustomEnchantmentAPI {

    /**
     * Registers a custom enchantment with the server.
     *
     * @param enchantment The custom enchantment to register.
     * @return true if the enchantment was registered successfully, false otherwise.
     */
    boolean registerEnchantment(CustomEnchantment enchantment);

    /**
     * Gets a registered custom enchantment by its key (namespace).
     *
     * @param namespace The namespace (key) of the enchantment.
     * @return The CustomEnchantment if found, null otherwise.
     */
    CustomEnchantment getEnchantment(String namespace);

    /**
     * Gets a registered custom enchantment by its name.
     *
     * @param name The display name of the enchantment.
     * @return The CustomEnchantment if found, null otherwise.
     */
    CustomEnchantment getEnchantmentByName(String name);
}