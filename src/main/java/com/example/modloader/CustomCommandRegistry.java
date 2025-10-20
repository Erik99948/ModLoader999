package com.example.modloader;

import com.example.modloader.api.ModCommandExecutor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class CustomCommandRegistry {

    private final Plugin plugin;
    private final Logger logger;
    private CommandMap commandMap;
    private final List<String> registeredCommandNames = new ArrayList<>();

    public CustomCommandRegistry(Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        try {
            if (plugin.getServer().getPluginManager() instanceof SimplePluginManager) {
                Field f = SimplePluginManager.class.getDeclaredField("commandMap");
                f.setAccessible(true);
                this.commandMap = (CommandMap) f.get(plugin.getServer().getPluginManager());
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            logger.severe("Failed to get Bukkit CommandMap: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void register(String commandName, ModCommandExecutor modExecutor) {
        if (commandMap == null) {
            logger.warning("CommandMap not available, cannot register command: " + commandName);
            return;
        }

        Command bukkitCommand = new Command(commandName) {
            @Override
            public boolean execute(CommandSender sender, String commandLabel, String[] args) {
                return modExecutor.onCommand(sender, commandLabel, args);
            }

            @Override
            public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
                List<String> completions = modExecutor.onTabComplete(sender, alias, args);
                return completions != null ? completions : super.tabComplete(sender, alias, args);
            }
        };

        commandMap.register(plugin.getName(), bukkitCommand);
        registeredCommandNames.add(commandName);
        logger.info("Registered custom command: /" + commandName);
    }

    public void unregisterAll() {
        if (commandMap == null) {
            return;
        }
        for (String commandName : registeredCommandNames) {
            Command command = commandMap.getCommand(commandName);
            if (command != null) {
                command.unregister(commandMap);
                logger.info("Unregistered custom command: /" + commandName);
            }
        }
        registeredCommandNames.clear();
    }
}
