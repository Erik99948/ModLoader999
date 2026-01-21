package com.example.modloader.api.block;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;

@FunctionalInterface
public interface BlockPlaceBehavior {
    void onPlace(BlockPlaceEvent event, Block placedBlock, Player placer);
}
