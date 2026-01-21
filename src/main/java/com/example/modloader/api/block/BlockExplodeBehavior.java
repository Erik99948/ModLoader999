package com.example.modloader.api.block;

import org.bukkit.block.Block;
import org.bukkit.event.block.BlockExplodeEvent;
import java.util.List;

@FunctionalInterface
public interface BlockExplodeBehavior {
    void onExplode(BlockExplodeEvent event, Block block, List<Block> affectedBlocks, float yield);
}
