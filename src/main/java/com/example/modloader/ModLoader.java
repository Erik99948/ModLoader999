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

        saveResource("mod.policy", false);

        this.modLoaderService = new ModLoaderService(this);

        this.modLoaderService.loadModsAndGeneratePack();

        if (!this.modLoaderService.getResourcePackGenerator().generate()) {
            getLogger().severe("Could not generate resource pack. Aborting resource pack server startup.");
            return;
        }

        saveDefaultConfig();
        int webServerPort = getConfig().getInt("web-server-port", 25566);

        this.webServer = new WebServer(this, this.modLoaderService, this.modLoaderService.getResourcePackGenerator().getZipFile(), webServerPort);
        this.webServer.start();


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
