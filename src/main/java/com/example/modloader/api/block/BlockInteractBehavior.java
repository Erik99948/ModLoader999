package com.example.modloader.api.block;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

public interface BlockInteractBehavior {
    void onInteract(PlayerInteractEvent event, Block interactedBlock, Player interactor);
}

