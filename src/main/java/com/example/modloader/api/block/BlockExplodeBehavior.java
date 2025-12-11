package com.example.modloader.api.block;

import org.bukkit.block.Block;
import org.bukkit.event.block.BlockExplodeEvent;

import java.util.List;

public interface BlockExplodeBehavior {
    void onExplode(BlockExplodeEvent event, Block block, List<Block> blocks, float yield);
}

