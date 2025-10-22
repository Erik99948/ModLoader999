package com.example.modloader;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ModLoaderCommandExecutor implements CommandExecutor, TabCompleter {

    private final JavaPlugin plugin;
    private final ModLoaderService modLoaderService;

    public ModLoaderCommandExecutor(JavaPlugin plugin, ModLoaderService modLoaderService) {
        this.plugin = plugin;
        this.modLoaderService = modLoaderService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "list":
                handleListCommand(sender);
                break;
            case "info":
                handleInfoCommand(sender, args);
                break;
            case "reload":
                if (!sender.hasPermission("modloader.reload")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                    return true;
                }
                sender.sendMessage(ChatColor.YELLOW + "Reloading ModLoader999 mods...");
                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                    modLoaderService.disableMods();
                    modLoaderService.loadModsAndGeneratePack();
                    sender.sendMessage(ChatColor.GREEN + "ModLoader999 mods reloaded successfully!");
                });
                break;
            case "enable":
                if (!sender.hasPermission("modloader.enable")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /modloader enable <modName>");
                    return true;
                }
                String modToEnable = args[1];
                sender.sendMessage(ChatColor.YELLOW + "Attempting to enable mod: " + modToEnable + "...");
                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                    try {
                        modLoaderService.enableMod(modToEnable);
                        sender.sendMessage(ChatColor.GREEN + "Mod '" + modToEnable + "' enabled successfully!");
                    } catch (Exception e) {
                        sender.sendMessage(ChatColor.RED + "Failed to enable mod '" + modToEnable + "': " + e.getMessage());
                        plugin.getLogger().severe("Error enabling mod '" + modToEnable + "': " + e.getMessage());
                        e.printStackTrace();
                    }
                });
                break;
            case "disable":
                if (!sender.hasPermission("modloader.disable")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /modloader disable <modName>");
                    return true;
                }
                String modToDisable = args[1];
                sender.sendMessage(ChatColor.YELLOW + "Attempting to disable mod: " + modToDisable + "...");
                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                    try {
                        modLoaderService.disableMod(modToDisable);
                        sender.sendMessage(ChatColor.GREEN + "Mod '" + modToDisable + "' disabled successfully!");
                    } catch (Exception e) {
                        sender.sendMessage(ChatColor.RED + "Failed to disable mod '" + modToDisable + "': " + e.getMessage());
                        plugin.getLogger().severe("Error disabling mod '" + modToDisable + "': " + e.getMessage());
                        e.printStackTrace();
                    }
                });
                break;
            case "unload":
                handleUnloadCommand(sender, args);
                break;
            case "load":
                handleLoadCommand(sender, args);
                break;
            case "help":
                sendHelpMessage(sender);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown command. Use /modloader help.");
                break;
        }

        return true;
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "--- ModLoader999 ---");
        sender.sendMessage(ChatColor.YELLOW + "/modloader list" + ChatColor.GRAY + " - List loaded mods.");
        sender.sendMessage(ChatColor.YELLOW + "/modloader info <modName>" + ChatColor.GRAY + " - Show detailed info about a mod.");
        sender.sendMessage(ChatColor.YELLOW + "/modloader reload" + ChatColor.GRAY + " - Reload all mods.");
        sender.sendMessage(ChatColor.YELLOW + "/modloader enable <modName>" + ChatColor.GRAY + " - Enable a disabled mod.");
        sender.sendMessage(ChatColor.YELLOW + "/modloader disable <modName>" + ChatColor.GRAY + " - Disable an enabled mod.");
        sender.sendMessage(ChatColor.YELLOW + "/modloader unload <modName>" + ChatColor.GRAY + " - Unload a mod temporarily.");
        sender.sendMessage(ChatColor.YELLOW + "/modloader load <modName>" + ChatColor.GRAY + " - Load a previously unloaded mod.");
        sender.sendMessage(ChatColor.YELLOW + "/modloader help" + ChatColor.GRAY + " - Show this help.");
    }

    private void handleListCommand(CommandSender sender) {
        List<ModInfo> loadedMods = modLoaderService.getLoadedModsInfo();
        if (loadedMods.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "No mods currently loaded.");
            return;
        }

        sender.sendMessage(ChatColor.GOLD + "--- Loaded Mods (" + loadedMods.size() + ") ---");
        for (ModInfo mod : loadedMods) {
            String status = modLoaderService.isModEnabled(mod.getName()) ? ChatColor.GREEN + "[ENABLED]" : ChatColor.RED + "[DISABLED]";
            sender.sendMessage(ChatColor.AQUA + mod.getName() + ChatColor.GRAY + " v" + mod.getVersion() + " by " + mod.getAuthor() + " " + status);
            if (!mod.getDependencies().isEmpty()) {
                sender.sendMessage(ChatColor.DARK_GRAY + "  Dependencies: " + String.join(", ", mod.getDependencies().keySet()));
            }
        }
    }

    private void handleInfoCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("modloader.info")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /modloader info <modName>");
            return;
        }
        String modName = args[1];
        ModInfo modInfo = modLoaderService.getModInfo(modName);
        if (modInfo == null) {
            sender.sendMessage(ChatColor.RED + "Mod '" + modName + "' not found.");
            return;
        }

        sender.sendMessage(ChatColor.GOLD + "--- Mod Info: " + modInfo.getName() + " ---");
        sender.sendMessage(ChatColor.YELLOW + "Version: " + ChatColor.WHITE + modInfo.getVersion());
        sender.sendMessage(ChatColor.YELLOW + "Author: " + ChatColor.WHITE + modInfo.getAuthor());
        sender.sendMessage(ChatColor.YELLOW + "Main Class: " + ChatColor.WHITE + modInfo.getMainClass());
        String status = modLoaderService.isModEnabled(modName) ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled";
        sender.sendMessage(ChatColor.YELLOW + "Status: " + status);
        if (!modInfo.getDependencies().isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "Dependencies:");
            for (Map.Entry<String, String> entry : modInfo.getDependencies().entrySet()) {
                sender.sendMessage(ChatColor.DARK_GRAY + "  - " + entry.getKey() + " (v" + entry.getValue() + ")");
            }
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Dependencies: " + ChatColor.WHITE + "None");
        }
    }

    private void handleUnloadCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("modloader.unload")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /modloader unload <modName>");
            return;
        }
        String modToUnload = args[1];
        sender.sendMessage(ChatColor.YELLOW + "Attempting to unload mod: " + modToUnload + "...");
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                modLoaderService.unloadMod(modToUnload);
                sender.sendMessage(ChatColor.GREEN + "Mod '" + modToUnload + "' unloaded successfully!");
            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "Failed to unload mod '" + modToUnload + "': " + e.getMessage());
                plugin.getLogger().severe("Error unloading mod '" + modToUnload + "': " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void handleLoadCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("modloader.load")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /modloader load <modName>");
            return;
        }
        String modToLoad = args[1];
        sender.sendMessage(ChatColor.YELLOW + "Attempting to load mod: " + modToLoad + "...");
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                modLoaderService.loadMod(modToLoad);
                sender.sendMessage(ChatColor.GREEN + "Mod '" + modToLoad + "' loaded successfully!");
            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "Failed to load mod '" + modToLoad + "': " + e.getMessage());
                plugin.getLogger().severe("Error loading mod '" + modToLoad + "': " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            return Arrays.asList("list", "info", "reload", "enable", "disable", "unload", "load", "help").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("enable") || subCommand.equals("load")) {
                return modLoaderService.getAvailableModNames().stream()
                        .filter(modName -> !modLoaderService.isModEnabled(modName))
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (subCommand.equals("disable") || subCommand.equals("unload") || subCommand.equals("info")) {
                return modLoaderService.getEnabledModNames().stream()
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }
}