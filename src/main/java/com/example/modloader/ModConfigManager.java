package com.example.modloader;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class ModConfigManager {

    private final JavaPlugin plugin;
    private final File configFolder;
    private final Map<String, YamlConfiguration> modConfigs = new HashMap<>();

    public ModConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.configFolder = new File(plugin.getDataFolder(), "configs");
        if (!this.configFolder.exists()) {
            this.configFolder.mkdirs();
        }
    }

    public YamlConfiguration getModConfig(String modId) {
        return modConfigs.get(modId);
    }

    public void loadModConfig(ModInfo modInfo) {
        File modConfigDir = new File(configFolder, modInfo.getName());
        if (!modConfigDir.exists()) {
            modConfigDir.mkdirs();
        }
        File modConfigFile = new File(modConfigDir, "config.yml");

        if (!modConfigFile.exists()) {
            try (InputStream defaultConfigFileStream = modInfo.getClassLoader().getResourceAsStream("config.yml")) {
                if (defaultConfigFileStream != null) {
                    try (java.io.FileOutputStream fos = new java.io.FileOutputStream(modConfigFile)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = defaultConfigFileStream.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                        }
                    }
                    plugin.getLogger().info("Extracted default config for mod: " + modInfo.getName());
                } else {
                    plugin.getLogger().info("Mod " + modInfo.getName() + " does not have a default config.yml.");
                }
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to extract default config for mod: " + modInfo.getName(), e);
            }
        }

        YamlConfiguration config = new YamlConfiguration();
        if (modConfigFile.exists()) {
            try {
                config.load(modConfigFile);
                modConfigs.put(modInfo.getName(), config);
                plugin.getLogger().info("Loaded config for mod: " + modInfo.getName());
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load config for mod: " + modInfo.getName(), e);
            }
        } else {
            modConfigs.put(modInfo.getName(), config);
            plugin.getLogger().info("No config file found for mod: " + modInfo.getName() + ". Using empty configuration.");
        }
    }

    public void saveModConfig(String modId) {
        YamlConfiguration config = modConfigs.get(modId);
        if (config != null) {
            File modConfigFile = new File(configFolder, modId + File.separator + "config.yml");
            try {
                config.save(modConfigFile);
                plugin.getLogger().info("Saved config for mod: " + modId);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save config for mod: " + modId, e);
            }
        }
    }

    public void unloadModConfig(String modId) {
        modConfigs.remove(modId);
        plugin.getLogger().info("Unloaded config for mod: " + modId);
    }
}