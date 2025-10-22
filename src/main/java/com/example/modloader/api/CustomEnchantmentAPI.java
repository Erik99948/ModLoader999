package com.example.modloader.api;

import com.example.modloader.CustomEnchantment;
import org.bukkit.enchantments.Enchantment;

public interface CustomEnchantmentAPI {

    boolean registerEnchantment(CustomEnchantment enchantment);

    CustomEnchantment getEnchantment(String namespace);

    CustomEnchantment getEnchantmentByName(String name);
}