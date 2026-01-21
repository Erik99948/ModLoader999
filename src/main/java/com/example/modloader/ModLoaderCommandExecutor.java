package com.example.modloader;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.*;
import java.util.stream.Collectors;

public class ModLoaderCommandExecutor implements CommandExecutor, TabCompleter {
    private final JavaPlugin plugin;
    private final ModLoaderService modLoaderService;
    private final WebServer webServer;

    public ModLoaderCommandExecutor(JavaPlugin plugin, ModLoaderService modLoaderService, WebServer webServer) {
        this.plugin = plugin;
        this.modLoaderService = modLoaderService;
        this.webServer = webServer;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("modloader.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "list":
                listMods(sender);
                break;
            case "info":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /modloader info <modId>");
                    return true;
                }
                showModInfo(sender, args[1]);
                break;
            case "reload":
                reloadMods(sender);
                break;
            case "resourcepack":
                sendResourcePackInfo(sender);
                break;
            case "apply":
                if (sender instanceof Player) {
                    applyResourcePack((Player) sender);
                } else {
                    sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                }
                break;
            case "help":
            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== ModLoader999 Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/modloader list" + ChatColor.WHITE + " - List all loaded mods");
        sender.sendMessage(ChatColor.YELLOW + "/modloader info <modId>" + ChatColor.WHITE + " - Show mod information");
        sender.sendMessage(ChatColor.YELLOW + "/modloader reload" + ChatColor.WHITE + " - Reload all mods");
        sender.sendMessage(ChatColor.YELLOW + "/modloader resourcepack" + ChatColor.WHITE + " - Show resource pack URL");
        sender.sendMessage(ChatColor.YELLOW + "/modloader apply" + ChatColor.WHITE + " - Apply resource pack (players only)");
    }

    private void listMods(CommandSender sender) {
        Map<String, ModInfo> mods = modLoaderService.getLoadedMods();
        
        if (mods.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "No mods are currently loaded.");
            return;
        }

        sender.sendMessage(ChatColor.GOLD + "=== Loaded Mods (" + mods.size() + ") ===");
        
        for (ModInfo mod : mods.values()) {
            ChatColor stateColor = getStateColor(mod.getState());
            sender.sendMessage(stateColor + "● " + ChatColor.WHITE + mod.getName() + 
                ChatColor.GRAY + " v" + mod.getVersion() + 
                ChatColor.DARK_GRAY + " [" + mod.getId() + "]");
        }
    }

    private void showModInfo(CommandSender sender, String modId) {
        ModInfo mod = modLoaderService.getMod(modId);
        
        if (mod == null) {
            sender.sendMessage(ChatColor.RED + "Mod not found: " + modId);
            return;
        }

        sender.sendMessage(ChatColor.GOLD + "=== " + mod.getName() + " ===");
        sender.sendMessage(ChatColor.YELLOW + "ID: " + ChatColor.WHITE + mod.getId());
        sender.sendMessage(ChatColor.YELLOW + "Version: " + ChatColor.WHITE + mod.getVersion());
        sender.sendMessage(ChatColor.YELLOW + "Author: " + ChatColor.WHITE + mod.getAuthor());
        sender.sendMessage(ChatColor.YELLOW + "Description: " + ChatColor.WHITE + mod.getDescription());
        sender.sendMessage(ChatColor.YELLOW + "State: " + getStateColor(mod.getState()) + mod.getState().name());
        sender.sendMessage(ChatColor.YELLOW + "API Version: " + ChatColor.WHITE + mod.getApiVersion());
        
        if (!mod.getDependencies().isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "Dependencies: " + ChatColor.WHITE + 
                String.join(", ", mod.getDependencies().keySet()));
        }
    }

    private void reloadMods(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "Reloading mods...");
        
        try {
            modLoaderService.disableMods();
            modLoaderService.loadModsAndGeneratePack();
            sender.sendMessage(ChatColor.GREEN + "Mods reloaded successfully!");
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Failed to reload mods: " + e.getMessage());
            plugin.getLogger().severe("Failed to reload mods: " + e.getMessage());
        }
    }

    private void sendResourcePackInfo(CommandSender sender) {
        String url = webServer.getResourcePackUrl();
        if (url != null) {
            sender.sendMessage(ChatColor.YELLOW + "Resource Pack URL: " + ChatColor.WHITE + url);
        } else {
            sender.sendMessage(ChatColor.RED + "Resource pack URL not available.");
        }
    }

    private void applyResourcePack(Player player) {
        String url = webServer.getResourcePackUrl();
        byte[] hash = webServer.getResourcePackHash();
        
        if (url == null) {
            player.sendMessage(ChatColor.RED + "Resource pack not available.");
            return;
        }

        player.sendMessage(ChatColor.YELLOW + "Applying resource pack...");
        
        if (hash != null && hash.length > 0) {
            player.setResourcePack(url, hash);
        } else {
            player.setResourcePack(url);
        }
    }

    private ChatColor getStateColor(ModState state) {
        switch (state) {
            case ENABLED: return ChatColor.GREEN;
            case DISABLED: return ChatColor.GRAY;
            case ERRORED: return ChatColor.RED;
            case LOADING:
            case INITIALIZING: return ChatColor.YELLOW;
            default: return ChatColor.WHITE;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("modloader.admin")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return Arrays.asList("list", "info", "reload", "resourcepack", "apply", "help")
                .stream()
                .filter(s -> s.startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("info")) {
            return modLoaderService.getLoadedMods().keySet()
                .stream()
                .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
