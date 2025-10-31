package com.example.examplemod;

import com.example.modloader.CustomBlock;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Furnace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;

public class DarkOreBlock extends CustomBlock {

    public DarkOreBlock(JavaPlugin plugin) {
        super(plugin, "dark_ore", Material.COAL_ORE);
    }

    @Override
    public String getName() {
        return "Dark Ore Block";
    }

    @Override
    public List<String> getLore() {
        return Collections.singletonList("§7A dark and mysterious ore.");
    }

    @Override
    public boolean onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();

        if (tool.getType().toString().endsWith("_PICKAXE") && tool.getType().getHardness() >= Material.STONE_PICKAXE.getHardness()) {
            event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), this.getItemStack());
            return true;
        }
        player.sendMessage("§cYou need a stone pickaxe or higher to mine this!");
        return false;
    }

    @Override
    public void onBlockPlace(BlockPlaceEvent event) {
    }

    @Override
    public float getHardness() {
        return Material.GOLD_ORE.getHardness();
    }

    @Override
    public String getToolType() {
        return "PICKAXE";
    }

    @Override
    public int getToolLevel() {
        return 2;
    }
}