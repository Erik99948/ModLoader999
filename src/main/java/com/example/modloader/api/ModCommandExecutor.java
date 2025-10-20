package com.example.modloader.api;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public interface ModCommandExecutor {

    boolean onCommand(CommandSender sender, String commandLabel, String[] args);

    List<String> onTabComplete(CommandSender sender, String alias, String[] args);
}
