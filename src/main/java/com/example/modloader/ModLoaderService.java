package com.example.modloader;

import com.example.modloader.api.*;
import com.example.modloader.api.event.EventBus;
import com.example.modloader.api.network.Networking;
import com.example.modloader.api.permissions.Permissions;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.jar.*;
import java.util.logging.Level;

public class ModLoaderService {
    private final JavaPlugin plugin;
    private final File modsFolder;
    private final Map<String, ModInfo> loadedMods = new LinkedHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final EventBus eventBus;
    private final Permissions permissions;
    private final Networking networking;
    private final ResourcePackGenerator resourcePackGenerator;
    private final AssetManager assetManager;

    public ModLoaderService(JavaPlugin plugin) {
        this.plugin = plugin;
        this.modsFolder = new File(plugin.getDataFolder(), "Mods");
        this.eventBus = new EventBus();
        this.permissions = new Permissions(plugin);
        this.networking = new Networking(plugin);
        this.assetManager = new AssetManager(plugin);
        this.resourcePackGenerator = new ResourcePackGenerator(plugin, assetManager);
    }

    public void loadModsAndGeneratePack() {
        discoverAndLoadMods();
        enableMods();
    }

    private void discoverAndLoadMods() {
        if (!modsFolder.exists()) {
            modsFolder.mkdirs();
        }
        
        File[] modFiles = modsFolder.listFiles((dir, name) -> name.endsWith(".modloader999") || name.endsWith(".jar"));
        if (modFiles == null || modFiles.length == 0) {
            plugin.getLogger().info("No mods found in Mods folder.");
            return;
        }

        for (File modFile : modFiles) {
            try {
                loadMod(modFile);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load mod: " + modFile.getName(), e);
            }
        }
    }

    private void loadMod(File modFile) throws Exception {
        try (JarFile jarFile = new JarFile(modFile)) {
            JarEntry modInfoEntry = jarFile.getJarEntry("modinfo.json");
            if (modInfoEntry == null) {
                plugin.getLogger().warning("Mod file " + modFile.getName() + " does not contain modinfo.json");
                return;
            }

            ModInfo modInfo;
            try (InputStream is = jarFile.getInputStream(modInfoEntry)) {
                JsonNode json = objectMapper.readTree(is);
                modInfo = parseModInfo(json, modFile);
            }

            if (loadedMods.containsKey(modInfo.getId())) {
                plugin.getLogger().warning("Mod with ID " + modInfo.getId() + " is already loaded.");
                return;
            }

            URL[] urls = { modFile.toURI().toURL() };
            URLClassLoader classLoader = new URLClassLoader(urls, plugin.getClass().getClassLoader());
            modInfo.setClassLoader(classLoader);

            Class<?> mainClass = classLoader.loadClass(modInfo.getMainClass());
            if (!ModInitializer.class.isAssignableFrom(mainClass)) {
                plugin.getLogger().warning("Main class " + modInfo.getMainClass() + " does not implement ModInitializer");
                return;
            }

            ModInitializer initializer = (ModInitializer) mainClass.getDeclaredConstructor().newInstance();
            modInfo.setInitializer(initializer);
            modInfo.setState(ModState.LOADED);
            loadedMods.put(modInfo.getId(), modInfo);

            plugin.getLogger().info("Loaded mod: " + modInfo.getName() + " v" + modInfo.getVersion() + " by " + modInfo.getAuthor());
        }
    }

    private ModInfo parseModInfo(JsonNode json, File modFile) {
        String id = json.has("id") ? json.get("id").asText() : "unknown";
        String name = json.has("name") ? json.get("name").asText() : id;
        String version = json.has("version") ? json.get("version").asText() : "1.0.0";
        String author = json.has("author") ? json.get("author").asText() : "Unknown";
        String description = json.has("description") ? json.get("description").asText() : "";
        String mainClass = json.has("main") ? json.get("main").asText() : "";
        String apiVersion = json.has("apiVersion") ? json.get("apiVersion").asText() : "1.0";

        Map<String, String> dependencies = new HashMap<>();
        if (json.has("dependencies")) {
            json.get("dependencies").fields().forEachRemaining(entry -> 
                dependencies.put(entry.getKey(), entry.getValue().asText())
            );
        }

        List<String> softDependencies = new ArrayList<>();
        if (json.has("softDependencies")) {
            json.get("softDependencies").forEach(node -> softDependencies.add(node.asText()));
        }

        return new ModInfo(id, name, version, author, description, mainClass, dependencies, softDependencies, new HashMap<>(), modFile, apiVersion);
    }

    private void enableMods() {
        for (ModInfo modInfo : loadedMods.values()) {
            try {
                modInfo.setState(ModState.INITIALIZING);
                ModAPI api = createModAPI(modInfo);
                
                modInfo.getInitializer().onPreLoad(api);
                modInfo.getInitializer().onLoad(api);
                modInfo.getInitializer().onPostLoad(api);
                modInfo.getInitializer().onEnable();
                
                modInfo.setState(ModState.ENABLED);
                plugin.getLogger().info("Enabled mod: " + modInfo.getName());
            } catch (Exception e) {
                modInfo.setState(ModState.ERRORED);
                plugin.getLogger().log(Level.SEVERE, "Failed to enable mod: " + modInfo.getName(), e);
            }
        }
    }

    private ModAPI createModAPI(ModInfo modInfo) {
        return new ModAPIImpl(plugin, modInfo, eventBus, permissions, networking, assetManager);
    }

    public void disableMods() {
        List<ModInfo> modsToDisable = new ArrayList<>(loadedMods.values());
        Collections.reverse(modsToDisable);

        for (ModInfo modInfo : modsToDisable) {
            try {
                if (modInfo.getState() == ModState.ENABLED) {
                    modInfo.getInitializer().onPreDisable();
                    modInfo.getInitializer().onDisable();
                    modInfo.getInitializer().onPostDisable();
                    modInfo.setState(ModState.DISABLED);
                    plugin.getLogger().info("Disabled mod: " + modInfo.getName());
                }
                
                if (modInfo.getClassLoader() != null) {
                    modInfo.getClassLoader().close();
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to disable mod: " + modInfo.getName(), e);
            }
        }
        
        loadedMods.clear();
        networking.shutdown();
    }

    public Map<String, ModInfo> getLoadedMods() {
        return Collections.unmodifiableMap(loadedMods);
    }

    public ModInfo getMod(String modId) {
        return loadedMods.get(modId);
    }

    public ResourcePackGenerator getResourcePackGenerator() {
        return resourcePackGenerator;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public AssetManager getAssetManager() {
        return assetManager;
    }
}
