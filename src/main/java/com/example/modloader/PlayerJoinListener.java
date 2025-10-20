package com.example.modloader;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerJoinListener implements Listener {

    private final JavaPlugin plugin;
    private final WebServer webServer;
    private final ResourcePackGenerator resourcePackGenerator;

    public PlayerJoinListener(JavaPlugin plugin, WebServer webServer, ResourcePackGenerator resourcePackGenerator) {
        this.plugin = plugin;
        this.webServer = webServer;
        this.resourcePackGenerator = resourcePackGenerator;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String url = webServer.getResourcePackUrl();
        String sha1 = resourcePackGenerator.getZipFileSha1();

        if (url == null || sha1 == null) {
            plugin.getLogger().warning("Resource pack URL or SHA-1 hash is not available. Cannot send to player.");
            return;
        }

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            player.setResourcePack(url, sha1);
            plugin.getLogger().info("Sent resource pack request to " + player.getName());
        }, 40L);
    }
}
