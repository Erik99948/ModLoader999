package com.example.modloader;

import com.example.modloader.api.config.ConfigChangeListener;
import com.example.modloader.api.config.ConfigValue;
import com.example.modloader.api.config.ModConfig;
import com.example.modloader.api.config.ModConfigProvider;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class ModConfigManager {

    private final JavaPlugin plugin;
    private final File configFolder;
    private final Map<String, ModConfig> modConfigs = new ConcurrentHashMap<>();
    private final Map<String, ConfigChangeListener<ModConfig>> configChangeListeners = new ConcurrentHashMap<>();
    private final Map<String, File> modConfigFiles = new ConcurrentHashMap<>();
    private final Map<String, Long> lastModifiedTimes = new ConcurrentHashMap<>();
    private BukkitRunnable configWatcherTask;

    public ModConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.configFolder = new File(plugin.getDataFolder(), "configs");
        if (!this.configFolder.exists()) {
            this.configFolder.mkdirs();
        }
        startConfigWatcher();
    }

    public <T extends ModConfig> T getModConfig(String modId, Class<T> configClass) {
        return configClass.cast(modConfigs.get(modId));
    }

    public <T extends ModConfig> void registerConfigChangeListener(String modId, ConfigChangeListener<T> listener) {
        configChangeListeners.put(modId, (ConfigChangeListener<ModConfig>) listener);
    }

    public void loadModConfig(ModInfo modInfo) {
        File modConfigDir = new File(configFolder, modInfo.getName());
        if (!modConfigDir.exists()) {
            modConfigDir.mkdirs();
        }
        File modConfigFile = new File(modConfigDir, "config.yml");
        modConfigFiles.put(modInfo.getName(), modConfigFile);

        // Extract default config.yml if it doesn't exist
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

        // Load YamlConfiguration
        YamlConfiguration yamlConfig = new YamlConfiguration();
        if (modConfigFile.exists()) {
            try {
                yamlConfig.load(modConfigFile);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load YAML config for mod: " + modInfo.getName(), e);
                return;
            }
        }

        // Find ModConfigProvider method in ModInitializer
        try {
            ModConfig modConfigInstance = null;
            for (Method method : modInfo.getInitializer().getClass().getMethods()) {
                if (method.isAnnotationPresent(ModConfigProvider.class) && ModConfig.class.isAssignableFrom(method.getReturnType())) {
                    modConfigInstance = (ModConfig) method.invoke(modInfo.getInitializer());
                    break;
                }
            }

            if (modConfigInstance != null) {
                // Populate ModConfig instance from YamlConfiguration
                loadConfigFromYaml(modConfigInstance, yamlConfig);
                modConfigs.put(modInfo.getName(), modConfigInstance);
                lastModifiedTimes.put(modInfo.getName(), modConfigFile.lastModified());
                plugin.getLogger().info("Loaded type-safe config for mod: " + modInfo.getName());
            } else {
                plugin.getLogger().info("Mod " + modInfo.getName() + " does not provide a type-safe config. Using raw YAML.");
                // Fallback to storing raw YamlConfiguration if no type-safe config is provided
                // This part needs adjustment if we strictly enforce type-safe configs
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to process type-safe config for mod: " + modInfo.getName(), e);
        }
    }

    private void loadConfigFromYaml(ModConfig configInstance, YamlConfiguration yamlConfig) throws IllegalAccessException {
        for (Field field : configInstance.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(ConfigValue.class)) {
                field.setAccessible(true);
                String path = field.getAnnotation(ConfigValue.class).value();
                if (path.isEmpty()) {
                    path = field.getName();
                }

                if (yamlConfig.contains(path)) {
                    Object value = yamlConfig.get(path);
                    // Basic type conversion (can be expanded)
                    if (field.getType().isInstance(value)) {
                        field.set(configInstance, value);
                    } else if (field.getType() == String.class) {
                        field.set(configInstance, String.valueOf(value));
                    } else if (field.getType() == int.class || field.getType() == Integer.class) {
                        field.set(configInstance, yamlConfig.getInt(path));
                    } else if (field.getType() == boolean.class || field.getType() == Boolean.class) {
                        field.set(configInstance, yamlConfig.getBoolean(path));
                    } else if (field.getType() == double.class || field.getType() == Double.class) {
                        field.set(configInstance, yamlConfig.getDouble(path));
                    } else if (field.getType() == long.class || field.getType() == Long.class) {
                        field.set(configInstance, yamlConfig.getLong(path));
                    } else {
                        plugin.getLogger().warning("Unsupported config type for field '" + field.getName() + "' in mod config.");
                    }
                }
            }
        }
    }

    public void saveModConfig(String modId) {
        ModConfig configInstance = modConfigs.get(modId);
        if (configInstance != null) {
            File modConfigFile = modConfigFiles.get(modId);
            if (modConfigFile == null) {
                plugin.getLogger().warning("Config file not found for mod: " + modId + ". Cannot save.");
                return;
            }
            YamlConfiguration yamlConfig = new YamlConfiguration();
            saveConfigToYaml(configInstance, yamlConfig);
            try {
                yamlConfig.save(modConfigFile);
                lastModifiedTimes.put(modId, modConfigFile.lastModified()); // Update timestamp after saving
                plugin.getLogger().info("Saved type-safe config for mod: " + modId);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save type-safe config for mod: " + modId, e);
            }
        }
    }

    public void unloadModConfig(String modId) {
        modConfigs.remove(modId);
        configChangeListeners.remove(modId);
        modConfigFiles.remove(modId);
        lastModifiedTimes.remove(modId);
        plugin.getLogger().info("Unloaded config for mod: " + modId);
    }

    private void saveConfigToYaml(ModConfig configInstance, YamlConfiguration yamlConfig) {
        for (Field field : configInstance.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(ConfigValue.class)) {
                field.setAccessible(true);
                String path = field.getAnnotation(ConfigValue.class).value();
                if (path.isEmpty()) {
                    path = field.getName();
                }

                try {
                    Object value = field.get(configInstance);
                    // Only save if the value is not null and is a primitive wrapper or String
                    if (value != null && (field.getType().isPrimitive() ||
                            value instanceof String ||
                            value instanceof Number ||
                            value instanceof Boolean)) {
                        yamlConfig.set(path, value);
                    } else if (value != null) {
                        plugin.getLogger().warning("Unsupported config type for saving field '" + field.getName() + "'. Skipping.");
                    }
                } catch (IllegalAccessException e) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to access field '" + field.getName() + "' for saving config.", e);
                }
            }
        }
    }


    private void startConfigWatcher() {
        configWatcherTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<String, File> entry : modConfigFiles.entrySet()) {
                    String modId = entry.getKey();
                    File configFile = entry.getValue();
                    long currentLastModified = configFile.lastModified();
                    if (lastModifiedTimes.containsKey(modId) && currentLastModified > lastModifiedTimes.get(modId)) {
                        plugin.getLogger().info("Config file for mod '" + modId + "' changed. Reloading...");
                        // Reload config
                        YamlConfiguration yamlConfig = new YamlConfiguration();
                        try {
                            yamlConfig.load(configFile);
                            ModConfig existingConfig = modConfigs.get(modId);
                            if (existingConfig != null) {
                                loadConfigFromYaml(existingConfig, yamlConfig);
                                lastModifiedTimes.put(modId, currentLastModified);
                                // Notify listener
                                ConfigChangeListener<ModConfig> listener = configChangeListeners.get(modId);
                                if (listener != null) {
                                    listener.onConfigChanged(existingConfig);
                                }
                                plugin.getLogger().info("Config for mod '" + modId + "' reloaded and listener notified.");
                            }
                        } catch (Exception e) {
                            plugin.getLogger().log(Level.SEVERE, "Failed to hot-reload config for mod: " + modId, e);
                        }
                    }
                }
            }
        };
        configWatcherTask.runTaskTimerAsynchronously(plugin, 20L * 5, 20L * 5); // Check every 5 seconds
    }

    public void stopConfigWatcher() {
        if (configWatcherTask != null) {
            configWatcherTask.cancel();
            configWatcherTask = null;
            plugin.getLogger().info("Config watcher stopped.");
        }
    }

    public String getModConfigYamlContent(String modId) {
        File modConfigFile = modConfigFiles.get(modId);
        if (modConfigFile != null && modConfigFile.exists()) {
            try {
                return new String(java.nio.file.Files.readAllBytes(modConfigFile.toPath()), StandardCharsets.UTF_8);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to read config.yml for mod: " + modId, e);
            }
        }
        return null;
    }

    public void setModConfigYamlContent(String modId, String yamlContent) {
        File modConfigFile = modConfigFiles.get(modId);
        if (modConfigFile != null) {
            try {
                java.nio.file.Files.write(modConfigFile.toPath(), yamlContent.getBytes(StandardCharsets.UTF_8));
                lastModifiedTimes.put(modId, modConfigFile.lastModified()); // Update timestamp to trigger reload
                plugin.getLogger().info("Updated config.yml for mod: " + modId + ". Reload will be triggered.");
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to write config.yml for mod: " + modId, e);
            }
        } else {
            plugin.getLogger().warning("Config file not found for mod: " + modId + ". Cannot set content.");
        }
    }
}
