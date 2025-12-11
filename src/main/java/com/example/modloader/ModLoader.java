package com.example.modloader;

import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;

public final class ModLoader extends JavaPlugin {

    private ModLoaderService modLoaderService;
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

        File publishedModsFolder = new File(getDataFolder(), "published_mods");
        if (!publishedModsFolder.exists()) {
            if (publishedModsFolder.mkdirs()) {
                getLogger().info("Created published_mods folder at: " + publishedModsFolder.getPath());
            } else {
                getLogger().severe("Failed to create published_mods folder!");
                return;
            }
        }

        saveResource("mod.policy", false);

        saveDefaultConfig();
        int webServerPort = getConfig().getInt("web-server-port", 25566);

        this.modLoaderService = new ModLoaderService(this);

        this.modLoaderService.loadModsAndGeneratePack();

        if (!this.modLoaderService.getResourcePackGenerator().generate()) {
            getLogger().severe("Could not generate resource pack. Aborting resource pack server startup.");
            return;
        }

        this.webServer = new WebServer(this, this.modLoaderService, this.modLoaderService.getResourcePackGenerator().getZipFile(), webServerPort, modsFolder);
        this.webServer.start();


        getCommand("modloader").setExecutor(new ModLoaderCommandExecutor(this, this.modLoaderService, this.webServer));


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

