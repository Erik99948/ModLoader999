package com.example.modloader;

import com.example.modloader.api.ModAPI;
import com.example.modloader.api.ModAPIImpl;
import com.example.modloader.api.ModInitializer;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ModLoaderService {

    private final JavaPlugin plugin;
    private final CustomItemRegistry itemRegistry;
    private final CustomMobRegistry mobRegistry;
    private final ModAPI modAPI;
    private final File resourceStagingDir;
    private final Map<String, ModInitializer> loadedMods = new HashMap<>();

    public ModLoaderService(JavaPlugin plugin) {
        this.plugin = plugin;
        this.itemRegistry = new CustomItemRegistry(plugin);
        this.mobRegistry = new CustomMobRegistry(plugin);
        this.modAPI = new ModAPIImpl(itemRegistry, mobRegistry);
        this.resourceStagingDir = new File(plugin.getDataFolder(), "resource-pack-staging");
    }

    public void loadModsAndGeneratePack() {
        // Clear and recreate the staging directory for a fresh build
        if (resourceStagingDir.exists()) {
            deleteDirectory(resourceStagingDir);
        }
        if (!resourceStagingDir.mkdirs()) {
            plugin.getLogger().severe("Failed to create resource pack staging directory.");
            return;
        }

        File modsFolder = new File(plugin.getDataFolder(), "Mods");
        if (!modsFolder.exists() || !modsFolder.isDirectory()) {
            plugin.getLogger().warning("Mods folder not found, skipping mod loading.");
            return;
        }

        File[] modFiles = modsFolder.listFiles((dir, name) -> name.endsWith(".modloader999"));
        if (modFiles == null || modFiles.length == 0) {
            plugin.getLogger().info("No .modloader999 mods found to load.");
            return;
        }

        for (File modFile : modFiles) {
            plugin.getLogger().info("Loading .modloader999 mod from: " + modFile.getName());
            try {
                loadModloader999(modFile);
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to load .modloader999 mod: " + modFile.getName());
                e.printStackTrace();
            }
        }

        plugin.getLogger().info("Finished processing mods. Resource pack assets staged.");
    }

    private void loadModloader999(File modFile) throws Exception {
        URL[] urls = {modFile.toURI().toURL()};
        // Use a new URLClassLoader for each mod to isolate them
        try (URLClassLoader classLoader = new URLClassLoader(urls, plugin.getClass().getClassLoader())) {
            try (JarFile jarFile = new JarFile(modFile)) {
                // 1. Read modinfo.json
                JarEntry modInfoEntry = jarFile.getJarEntry("modinfo.json");
                if (modInfoEntry == null) {
                    plugin.getLogger().warning("Mod " + modFile.getName() + " is missing modinfo.json. Skipping.");
                    return;
                }

                String mainClassName;
                String modName;
                try (InputStream is = jarFile.getInputStream(modInfoEntry);
                     InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                    Gson gson = new Gson();
                    JsonObject modInfo = gson.fromJson(reader, JsonObject.class);
                    mainClassName = modInfo.get("main").getAsString();
                    modName = modInfo.get("name").getAsString();
                }

                // 2. Load main class and initialize mod
                Class<?> mainClass = classLoader.loadClass(mainClassName);
                if (ModInitializer.class.isAssignableFrom(mainClass) && !mainClass.isInterface()) {
                    plugin.getLogger().info("Found ModInitializer: " + mainClass.getName() + " for mod " + modName);
                    ModInitializer modInitializer = (ModInitializer) mainClass.getDeclaredConstructor().newInstance();
                    
                    // Store the mod initializer for later management (e.g., onDisable, hot-reload)
                    loadedMods.put(modName, modInitializer);

                    // Call onLoad and onEnable
                    modInitializer.onLoad(modAPI);
                    modInitializer.onEnable();
                    plugin.getLogger().info("Mod " + modName + " initialized and enabled.");
                } else {
                    plugin.getLogger().warning("Main class " + mainClassName + " in mod " + modFile.getName() + " does not implement ModInitializer. Skipping.");
                }

                // 3. Extract resources
                jarFile.stream().forEach(entry -> {
                    String entryName = entry.getName();
                    if (!entry.isDirectory() && entryName.startsWith("assets/")) {
                        try (InputStream entryIs = jarFile.getInputStream(entry)) {
                            extractResource(entryName, entryIs);
                        } catch (Exception e) {
                            plugin.getLogger().severe("Failed to extract resource " + entryName + " from " + modFile.getName());
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }

    private void extractResource(String resourcePath, InputStream inputStream) {
        File outputFile = new File(resourceStagingDir, resourcePath);
        outputFile.getParentFile().mkdirs(); // Ensure parent directories exist

        try (OutputStream out = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            plugin.getLogger().info("Extracted resource: " + resourcePath);

        } catch (Exception e) {
            plugin.getLogger().severe("Failed to extract resource: " + resourcePath);
            e.printStackTrace();
        }
    }

    public void disableMods() {
        for (Map.Entry<String, ModInitializer> entry : loadedMods.entrySet()) {
            plugin.getLogger().info("Disabling mod: " + entry.getKey());
            try {
                entry.getValue().onDisable();
            } catch (Exception e) {
                plugin.getLogger().severe("Error disabling mod " + entry.getKey());
                e.printStackTrace();
            }
        }
        loadedMods.clear();
    }

    private boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            } 
        }
        return directoryToBeDeleted.delete();
    }
}
