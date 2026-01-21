package com.example.modloader.api.block;

import org.bukkit.block.Block;
import org.bukkit.event.block.BlockRedstoneEvent;

@FunctionalInterface
public interface BlockRedstoneBehavior {
    void onRedstone(BlockRedstoneEvent event, Block block, int oldCurrent, int newCurrent);
}
