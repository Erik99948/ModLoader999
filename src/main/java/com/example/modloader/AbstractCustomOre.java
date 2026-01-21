package com.example.modloader;

import com.example.modloader.api.block.BlockBreakBehavior;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Abstract base class for custom ore blocks.
 * Provides default mining behavior based on tool level.
 */
public abstract class AbstractCustomOre extends CustomBlock {
    
    public AbstractCustomOre(String id, Material baseMaterial, int customModelData, String displayName, List<String> lore,
                             int requiredMiningLevel, Material dropMaterial, int minDrop, int maxDrop) {
        super(id, baseMaterial, customModelData, displayName, lore,
                null,
                new DefaultOreBreakBehavior(requiredMiningLevel, dropMaterial, minDrop, maxDrop),
                null,
                null,
                null,
                Collections.emptyList());
    }

    private static class DefaultOreBreakBehavior implements BlockBreakBehavior {
        private final int requiredMiningLevel;
        private final Material dropMaterial;
        private final int minDrop;
        private final int maxDrop;

        public DefaultOreBreakBehavior(int requiredMiningLevel, Material dropMaterial, int minDrop, int maxDrop) {
            this.requiredMiningLevel = requiredMiningLevel;
            this.dropMaterial = dropMaterial;
            this.minDrop = minDrop;
            this.maxDrop = maxDrop;
        }

        @Override
        public void onBreak(BlockBreakEvent event, Block brokenBlock, Player breaker) {
            ItemStack tool = breaker.getInventory().getItemInMainHand();
            int toolMiningLevel = getToolMiningLevel(tool.getType());
            
            if (toolMiningLevel >= requiredMiningLevel) {
                event.setDropItems(false);
                Random random = new Random();
                int dropAmount = random.nextInt(maxDrop - minDrop + 1) + minDrop;
                brokenBlock.getWorld().dropItemNaturally(brokenBlock.getLocation(), new ItemStack(dropMaterial, dropAmount));
            } else {
                breaker.sendMessage("§cYou need a pickaxe of level " + requiredMiningLevel + " or higher to mine this!");
                event.setCancelled(true);
            }
        }

        private int getToolMiningLevel(Material toolMaterial) {
            switch (toolMaterial) {
                case WOODEN_PICKAXE: return 1;
                case STONE_PICKAXE: return 2;
                case IRON_PICKAXE: return 3;
                case DIAMOND_PICKAXE: return 4;
                case NETHERITE_PICKAXE: return 5;
                default: return 0;
            }
        }
    }
}
