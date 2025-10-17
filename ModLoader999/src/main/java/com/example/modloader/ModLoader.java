package com.example.modloader;

import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;

public final class ModLoader extends JavaPlugin {

    private ModLoaderService modLoaderService;
    private ResourcePackGenerator resourcePackGenerator;
    private WebServer webServer;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("ModLoader plugin is enabling!");

        // Create the Mods directory
        File modsFolder = new File(getDataFolder(), "Mods");
        if (!modsFolder.exists()) {
            if (modsFolder.mkdirs()) {
                getLogger().info("Created Mods folder at: " + modsFolder.getPath());
            } else {
                getLogger().severe("Failed to create Mods folder!");
                return; // Don't continue if we can't create the folder
            }
        }

        // Initialize services
        this.modLoaderService = new ModLoaderService(this);
        this.resourcePackGenerator = new ResourcePackGenerator(this);

        // Load mods and extract resources
        this.modLoaderService.loadModsAndGeneratePack();

        // Generate the resource pack zip. If it fails, don't start the web server.
        if (!this.resourcePackGenerator.generate()) {
            getLogger().severe("Could not generate resource pack. Aborting resource pack server startup.");
            return;
        }

        // Load web server port from config, or use default
        saveDefaultConfig(); // Ensure config.yml exists
        int webServerPort = getConfig().getInt("web-server-port", 25566);

        // Start the web server to host the pack
        this.webServer = new WebServer(this, this.resourcePackGenerator.getZipFile(), webServerPort);
        this.webServer.start();

        // Register player join listener
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this, webServer, resourcePackGenerator), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("ModLoader plugin is disabling!");

        // Stop the web server
        if (this.webServer != null) {
            this.webServer.stop();
        }

        // Disable loaded mods
        if (this.modLoaderService != null) {
            this.modLoaderService.disableMods();
        }
    }
}
