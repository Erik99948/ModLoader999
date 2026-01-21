package com.example.examplemod;

import com.example.modloader.api.ModAPI;
import com.example.modloader.api.ModInitializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class ExampleMod implements ModInitializer {
    
    private static final Logger LOGGER = Logger.getLogger("ExampleMod");
    private ModAPI api;
    
    @Override
    public void onPreLoad(ModAPI api) {
        LOGGER.info("ExampleMod is pre-loading...");
    }
    
    @Override
    public void onLoad(ModAPI api) {
        this.api = api;
        LOGGER.info("ExampleMod is loading...");
        
        registerItems();
        registerCommands();
        registerListeners();
    }
    
    @Override
    public void onPostLoad(ModAPI api) {
        LOGGER.info("ExampleMod post-load complete!");
    }
    
    @Override
    public void onEnable() {
        LOGGER.info("=================================");
        LOGGER.info("  ExampleMod has been enabled!");
        LOGGER.info("  Use /examplemod for commands");
        LOGGER.info("=================================");
    }
    
    @Override
    public void onDisable() {
        LOGGER.info("ExampleMod has been disabled!");
    }
    
    private void registerItems() {
        ItemStack magicWand = createMagicWand();
        api.registerItem("magic_wand", magicWand);
        LOGGER.info("Registered item: magic_wand");
        
        ItemStack luckyDiamond = createLuckyDiamond();
        api.registerItem("lucky_diamond", luckyDiamond);
        LOGGER.info("Registered item: lucky_diamond");
    }
    
    private ItemStack createMagicWand() {
        ItemStack magicWand = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = magicWand.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "Magic Wand");
            meta.setLore(Arrays.asList(
                ChatColor.GRAY + "A powerful magical artifact",
                "",
                ChatColor.YELLOW + "Right-click to cast a spell!",
                "",
                ChatColor.DARK_PURPLE + "Created by ExampleMod"
            ));
            meta.setCustomModelData(10001);
            magicWand.setItemMeta(meta);
        }
        return magicWand;
    }
    
    private ItemStack createLuckyDiamond() {
        ItemStack luckyDiamond = new ItemStack(Material.DIAMOND);
        ItemMeta meta = luckyDiamond.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + "Lucky Diamond");
            meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Brings good fortune",
                ChatColor.GREEN + "+10% Luck"
            ));
            meta.setCustomModelData(10002);
            luckyDiamond.setItemMeta(meta);
        }
        return luckyDiamond;
    }
    
    private void registerCommands() {
        api.registerCommand("examplemod", new ExampleModCommand());
        LOGGER.info("Registered command: /examplemod");
    }
    
    private void registerListeners() {
        api.registerListener(new ExampleModListener());
        LOGGER.info("Registered event listeners");
    }
    
    private class ExampleModCommand implements com.example.modloader.api.ModCommandExecutor {
        
        @Override
        public boolean onCommand(CommandSender sender, String commandLabel, String[] args) {
            if (args.length == 0) {
                sendHelp(sender);
                return true;
            }
            
            String subCommand = args[0].toLowerCase();
            
            switch (subCommand) {
                case "help":
                    sendHelp(sender);
                    break;
                    
                case "give":
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
                        return true;
                    }
                    if (args.length < 2) {
                        sender.sendMessage(ChatColor.RED + "Usage: /examplemod give <item>");
                        sender.sendMessage(ChatColor.YELLOW + "Available items: magic_wand, lucky_diamond");
                        return true;
                    }
                    giveItem((Player) sender, args[1]);
                    break;
                    
                case "info":
                    sender.sendMessage(ChatColor.GOLD + "=== ExampleMod Info ===");
                    sender.sendMessage(ChatColor.YELLOW + "Version: " + ChatColor.WHITE + "1.0.0");
                    sender.sendMessage(ChatColor.YELLOW + "Author: " + ChatColor.WHITE + "YourName");
                    sender.sendMessage(ChatColor.YELLOW + "API Version: " + ChatColor.WHITE + "1.0");
                    break;
                    
                case "broadcast":
                    if (args.length < 2) {
                        sender.sendMessage(ChatColor.RED + "Usage: /examplemod broadcast <message>");
                        return true;
                    }
                    String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                    Bukkit.broadcastMessage(ChatColor.GOLD + "[ExampleMod] " + ChatColor.WHITE + message);
                    break;
                    
                default:
                    sender.sendMessage(ChatColor.RED + "Unknown sub-command: " + subCommand);
                    sendHelp(sender);
                    break;
            }
            
            return true;
        }
        
        @Override
        public List<String> onTabComplete(CommandSender sender, String alias, String[] args) {
            if (args.length == 1) {
                return Arrays.asList("help", "give", "info", "broadcast");
            }
            if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
                return Arrays.asList("magic_wand", "lucky_diamond");
            }
            return Arrays.asList();
        }
        
        private void sendHelp(CommandSender sender) {
            sender.sendMessage(ChatColor.GOLD + "=== ExampleMod Commands ===");
            sender.sendMessage(ChatColor.YELLOW + "/examplemod help" + ChatColor.WHITE + " - Show this help");
            sender.sendMessage(ChatColor.YELLOW + "/examplemod give <item>" + ChatColor.WHITE + " - Give yourself an item");
            sender.sendMessage(ChatColor.YELLOW + "/examplemod info" + ChatColor.WHITE + " - Show mod info");
            sender.sendMessage(ChatColor.YELLOW + "/examplemod broadcast <msg>" + ChatColor.WHITE + " - Broadcast a message");
        }
        
        private void giveItem(Player player, String itemName) {
            ItemStack item = null;
            
            switch (itemName.toLowerCase()) {
                case "magic_wand":
                    item = createMagicWand();
                    break;
                case "lucky_diamond":
                    item = createLuckyDiamond();
                    break;
                default:
                    player.sendMessage(ChatColor.RED + "Unknown item: " + itemName);
                    player.sendMessage(ChatColor.YELLOW + "Available items: magic_wand, lucky_diamond");
                    return;
            }
            
            if (item != null) {
                player.getInventory().addItem(item);
                player.sendMessage(ChatColor.GREEN + "You received: " + item.getItemMeta().getDisplayName());
            }
        }
    }
    
    private class ExampleModListener implements Listener {
        
        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent event) {
            Player player = event.getPlayer();
            
            Bukkit.getScheduler().runTaskLater(api.getPlugin(), () -> {
                player.sendMessage("");
                player.sendMessage(ChatColor.GOLD + "Welcome! " + ChatColor.YELLOW + "This server uses ExampleMod");
                player.sendMessage(ChatColor.GRAY + "Type " + ChatColor.YELLOW + "/examplemod help" + ChatColor.GRAY + " to get started!");
                player.sendMessage("");
            }, 20L);
        }
    }
}
