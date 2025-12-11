package com.example.modloader;

import com.example.modloader.config.ConfigurationSource;
import com.example.modloader.config.YamlConfigurationSource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import com.example.modloader.api.config.ConfigChangeListener;
import com.example.modloader.api.config.ConfigProperty;
import com.example.modloader.api.config.ModConfig;
import com.example.modloader.api.config.ModConfigProvider;

public class ModConfigManager {

    private final JavaPlugin plugin;
    private final File configFolder;
    private final Map<String, ModConfig> modConfigs = new ConcurrentHashMap<>();
    private final Map<String, ConfigChangeListener<ModConfig>> configChangeListeners = new ConcurrentHashMap<>();
    private final Map<String, File> modConfigFiles = new ConcurrentHashMap<>();
    private final Map<String, Long> lastModifiedTimes = new ConcurrentHashMap<>();
    private BukkitRunnable configWatcherTask;

    private final ConfigurationSource configSource;

    public ModConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.configFolder = new File(plugin.getDataFolder(), "configs");
        if (!this.configFolder.exists()) {
            this.configFolder.mkdirs();
        }
        this.configSource = new YamlConfigurationSource();
        startConfigWatcher();
    }

    public <T extends ModConfig> T getModConfig(String modId, Class<T> configClass) {
        plugin.getLogger().info("Attempting to retrieve config for modId: " + modId);
        ModConfig config = modConfigs.get(modId);
        if (config == null) {
            plugin.getLogger().warning("Config for modId: " + modId + " not found in modConfigs map.");
        } else {
            plugin.getLogger().info("Config for modId: " + modId + " found. Type: " + config.getClass().getName());
        }
        return configClass.cast(config);
    }

    public <T extends ModConfig> void registerConfigChangeListener(String modId, ConfigChangeListener<T> listener) {
        configChangeListeners.put(modId, (ConfigChangeListener<ModConfig>) listener);
    }

    public void loadModConfig(ModInfo modInfo) {
        File modConfigDir = new File(configFolder, modInfo.getId());
        if (!modConfigDir.exists()) {
            modConfigDir.mkdirs();
        }
        File modConfigFile = new File(modConfigDir, "config.yml");
        modConfigFiles.put(modInfo.getId(), modConfigFile);


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


        JsonNode rootNode = null;
        if (modConfigFile.exists()) {
            try (InputStream is = new java.io.FileInputStream(modConfigFile)) {
                rootNode = configSource.load(is);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load config file for mod: " + modInfo.getName(), e);
                return;
            }
        }


        try {
            ModConfig modConfigInstance = null;
            plugin.getLogger().info("Searching for @ModConfigProvider in mod: " + modInfo.getName());
            for (Method method : modInfo.getInitializer().getClass().getMethods()) {
                if (method.isAnnotationPresent(ModConfigProvider.class) && ModConfig.class.isAssignableFrom(method.getReturnType())) {
                    plugin.getLogger().info("Found @ModConfigProvider method: " + method.getName() + " in mod: " + modInfo.getName());
                    if (java.lang.reflect.Modifier.isStatic(method.getModifiers())) {

                        plugin.getLogger().info("Invoked static @ModConfigProvider method for mod: " + modInfo.getName());
                    } else {

                        plugin.getLogger().info("Invoked instance @ModConfigProvider method for mod: " + modInfo.getName());
                    }
                    break;
                }
            }

            if (modConfigInstance != null) {
                plugin.getLogger().info("ModConfig instance created for mod: " + modInfo.getName());

                loadConfigFromJsonNode(modConfigInstance, rootNode);
                modConfigs.put(modInfo.getId(), modConfigInstance);
                plugin.getLogger().info("Config for modId: " + modInfo.getId() + " successfully added to modConfigs map.");
                lastModifiedTimes.put(modInfo.getId(), modConfigFile.lastModified());
                plugin.getLogger().info("Loaded type-safe config for mod: " + modInfo.getName());
            } else {
                plugin.getLogger().warning("Mod " + modInfo.getName() + " does not provide a type-safe config. Using raw YAML.");


            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to process type-safe config for mod: " + modInfo.getName(), e);
        }
    }

    private void loadConfigFromJsonNode(ModConfig configInstance, JsonNode jsonNode) throws IllegalAccessException {
        if (jsonNode == null) {
            return;
        }
        for (Field field : configInstance.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(ConfigProperty.class)) {
                field.setAccessible(true);
                ConfigProperty configProperty = field.getAnnotation(ConfigProperty.class);
                String path = configProperty.path();
                if (path.isEmpty()) {
                    path = field.getName();
                }

                JsonNode valueNode = jsonNode.get(path);


                if (configProperty.required() && valueNode == null && configProperty.defaultValue().isEmpty()) {
                    plugin.getLogger().warning("Required config property '" + path + "' for mod config is missing and no default value is provided.");
                    continue;
                }


                if (valueNode == null || valueNode.isNull()) {
                    if (!configProperty.defaultValue().isEmpty()) {

                        try {
                            Object defaultValue = parseDefaultValue(configProperty.defaultValue(), field.getType());
                            field.set(configInstance, defaultValue);
                            plugin.getLogger().info("Using default value for '" + path + "': " + configProperty.defaultValue());
                        } catch (IllegalArgumentException | ClassCastException e) {
                            plugin.getLogger().log(Level.WARNING, "Failed to parse default value '" + configProperty.defaultValue() + "' for field '" + field.getName() + "'.", e);
                        }
                    }
                    continue;
                }


                try {
                    if (field.getType() == String.class) {
                        String value = valueNode.asText();

                        if (!configProperty.pattern().isEmpty() && !value.matches(configProperty.pattern())) {
                            plugin.getLogger().warning("Config property '" + path + "' does not match required pattern: " + configProperty.pattern());
                            continue;
                        }

                        if (configProperty.allowedValues().length > 0 && !java.util.Arrays.asList(configProperty.allowedValues()).contains(value)) {
                            plugin.getLogger().warning("Config property '" + path + "' has an invalid value. Allowed values: " + java.util.Arrays.toString(configProperty.allowedValues()));
                            continue;
                        }
                        field.set(configInstance, value);
                    } else if (field.getType() == int.class || field.getType() == Integer.class) {
                        int value = valueNode.asInt();
                        if (value < configProperty.minValue() || value > configProperty.maxValue()) {
                            plugin.getLogger().warning("Config property '" + path + "' is out of range. Min: " + configProperty.minValue() + ", Max: " + configProperty.maxValue());
                            continue;
                        }
                        field.set(configInstance, value);
                    } else if (field.getType() == boolean.class || field.getType() == Boolean.class) {
                        field.set(configInstance, valueNode.asBoolean());
                    } else if (field.getType() == double.class || field.getType() == Double.class) {
                        double value = valueNode.asDouble();
                        if (value < configProperty.minValue() || value > configProperty.maxValue()) {
                            plugin.getLogger().warning("Config property '" + path + "' is out of range. Min: " + configProperty.minValue() + ", Max: " + configProperty.maxValue());
                            continue;
                        }
                        field.set(configInstance, value);
                    } else if (field.getType() == long.class || field.getType() == Long.class) {
                        long value = valueNode.asLong();
                        if (value < configProperty.minValue() || value > configProperty.maxValue()) {
                            plugin.getLogger().warning("Config property '" + path + "' is out of range. Min: " + configProperty.minValue() + ", Max: " + configProperty.maxValue());
                            continue;
                        }
                        field.set(configInstance, value);
                    } else {
                        plugin.getLogger().warning("Unsupported config type for field '" + field.getName() + "' in mod config. Path: " + path);
                    }
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Error setting config property '" + path + "' for field '" + field.getName() + "'.", e);
                }
            }
        }
    }


    private Object parseDefaultValue(String defaultValue, Class<?> type) {
        if (type == String.class) {
            return defaultValue;
        } else if (type == int.class || type == Integer.class) {
            return Integer.parseInt(defaultValue);
        } else if (type == boolean.class || type == Boolean.class) {
            return Boolean.parseBoolean(defaultValue);
        } else if (type == double.class || type == Double.class) {
            return Double.parseDouble(defaultValue);
        } else if (type == long.class || type == Long.class) {
            return Long.parseLong(defaultValue);
        } else {
            throw new IllegalArgumentException("Unsupported type for default value parsing: " + type.getName());
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
            ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
            ObjectNode rootNode = objectMapper.createObjectNode();
            saveConfigToJsonNode(configInstance, rootNode);
            try (OutputStream os = new java.io.FileOutputStream(modConfigFile)) {
                configSource.save(rootNode, os);
                lastModifiedTimes.put(modId, modConfigFile.lastModified());
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

    private void saveConfigToJsonNode(ModConfig configInstance, ObjectNode objectNode) {
        for (Field field : configInstance.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(ConfigProperty.class)) {
                field.setAccessible(true);
                ConfigProperty configProperty = field.getAnnotation(ConfigProperty.class);
                String path = configProperty.path();
                if (path.isEmpty()) {
                    path = field.getName();
                }

                try {
                    Object value = field.get(configInstance);
                    if (value != null) {
                        if (value instanceof String) {
                            objectNode.put(path, (String) value);
                        } else if (value instanceof Integer) {
                            objectNode.put(path, (Integer) value);
                        } else if (value instanceof Boolean) {
                            objectNode.put(path, (Boolean) value);
                        } else if (value instanceof Double) {
                            objectNode.put(path, (Double) value);
                        } else if (value instanceof Long) {
                            objectNode.put(path, (Long) value);
                        } else {
                            plugin.getLogger().warning("Unsupported config type for saving field '" + field.getName() + "'. Skipping. Path: " + path);
                        }
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

                        JsonNode rootNode = null;
                        ModConfig existingConfig = modConfigs.get(modId);
                        if (existingConfig != null) {
                            try (InputStream is = new java.io.FileInputStream(configFile)) {
                                rootNode = configSource.load(is);
                                loadConfigFromJsonNode(existingConfig, rootNode);
                                lastModifiedTimes.put(modId, currentLastModified);

                                ConfigChangeListener<ModConfig> listener = configChangeListeners.get(modId);
                                if (listener != null) {
                                    listener.onConfigChanged(existingConfig);
                                }
                                plugin.getLogger().info("Config for mod '" + modId + "' reloaded and listener notified.");
                            } catch (IOException e) {
                                plugin.getLogger().log(Level.SEVERE, "Failed to load config file for mod during hot-reload: " + modId, e);
                            } catch (IllegalAccessException e) {
                                plugin.getLogger().log(Level.SEVERE, "Failed to access config fields for mod during hot-reload: " + modId, e);
                            }
                        }
                    }
                }
            }
        };

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
                lastModifiedTimes.put(modId, modConfigFile.lastModified());
                plugin.getLogger().info("Updated config.yml for mod: " + modId + ". Reload will be triggered.");
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to write config.yml for mod: " + modId, e);
            }
        } else {
            plugin.getLogger().warning("Config file not found for mod: " + modId + ". Cannot set content.");
        }
    }
}

