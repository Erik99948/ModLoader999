package com.example.modloader.api.block;

import org.bukkit.block.Block;
import org.bukkit.event.block.BlockRedstoneEvent;

public interface BlockRedstoneBehavior {
    void onRedstone(BlockRedstoneEvent event, Block block, int oldCurrent, int newCurrent);
}
