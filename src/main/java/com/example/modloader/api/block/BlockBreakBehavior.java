package com.example.modloader.api.block;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

public interface BlockBreakBehavior {
    void onBreak(BlockBreakEvent event, Block brokenBlock, Player breaker);
}

