package com.example.examplemod;

import com.example.modloader.AbstractCustomOre;
import org.bukkit.Material;

import java.util.Collections;

public class DarkOreBlock extends AbstractCustomOre {

    public DarkOreBlock() {
        super("dark_ore", Material.COAL_ORE, 0, "Dark Ore Block",
                Collections.singletonList("§7A dark and mysterious ore."),
                2,
                Material.COAL,
                1, 1);
    }
}