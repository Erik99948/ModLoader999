package com.example.modloader;

import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;

public final class ModLoader extends JavaPlugin {

    private ModLoaderService modLoaderService;
    private ResourcePackGenerator resourcePackGenerator;
    private WebServer webServer;

    @Override
    public void onEnable() {
        getLogger().info("ModLoader plugin is enabling!");

        File modsFolder = new File(getDataFolder(), "Mods");
        if (!modsFolder.exists()) {
            if (modsFolder.mkdirs()) {
                getLogger().info("Created Mods folder at: " + modsFolder.getPath());
            } else {
                getLogger().severe("Failed to create Mods folder!");
                return;
            }
        }

        this.modLoaderService = new ModLoaderService(this);
        this.resourcePackGenerator = new ResourcePackGenerator(this);

        this.modLoaderService.loadModsAndGeneratePack();

        if (!this.resourcePackGenerator.generate()) {
            getLogger().severe("Could not generate resource pack. Aborting resource pack server startup.");
            return;
        }

        saveDefaultConfig();
        int webServerPort = getConfig().getInt("web-server-port", 25566);

        this.webServer = new WebServer(this, this.resourcePackGenerator.getZipFile(), webServerPort);
        this.webServer.start();

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this, webServer, resourcePackGenerator), this);

        // Register CustomInventoryListener
        new CustomInventoryListener((com.example.modloader.api.CustomInventoryAPIImpl) modLoaderService.getModAPI().getCustomInventoryAPI(), this);

        getCommand("modloader").setExecutor(new ModLoaderCommandExecutor(this, modLoaderService));
        getCommand("modloader").setTabCompleter(new ModLoaderCommandExecutor(this, modLoaderService));
    }

    @Override
    public void onDisable() {
        getLogger().info("ModLoader plugin is disabling!");

        if (this.webServer != null) {
            this.webServer.stop();
        }

        if (this.modLoaderService != null) {
            this.modLoaderService.disableMods();
        }
    }
}
