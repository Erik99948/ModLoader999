package com.example.modloader.api.block;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * Functional interface for custom block break behavior.
 */
@FunctionalInterface
public interface BlockBreakBehavior {
    /**
     * Called when a custom block is broken.
     *
     * @param event The block break event
     * @param brokenBlock The block that was broken
     * @param breaker The player who broke the block
     */
    void onBreak(BlockBreakEvent event, Block brokenBlock, Player breaker);
}
