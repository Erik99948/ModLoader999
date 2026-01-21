package com.example.modloader.api;

import org.bukkit.command.CommandSender;
import java.util.Collections;
import java.util.List;

/**
 * Interface for mod command executors.
 */
public interface ModCommandExecutor {
    boolean onCommand(CommandSender sender, String commandLabel, String[] args);
    
    default List<String> onTabComplete(CommandSender sender, String alias, String[] args) {
        return Collections.emptyList();
    }
}
