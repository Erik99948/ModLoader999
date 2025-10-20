package com.example.modloader;

import com.example.modloader.api.ModAPI;
import com.example.modloader.api.ModAPIImpl;
import com.example.modloader.api.ModInitializer;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
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
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class ModLoaderService {

    private final JavaPlugin plugin;
    private final CustomItemRegistry itemRegistry;
    private final CustomMobRegistry mobRegistry;
    private final CustomBlockRegistry blockRegistry;
    private final CustomCommandRegistry commandRegistry;
    private final CustomEventListenerRegistry eventListenerRegistry;
    private final CustomRecipeRegistry recipeRegistry;
    private final CustomWorldGeneratorRegistry worldGeneratorRegistry;
    private final ModAPI modAPI;
    private final File resourceStagingDir;

    private final Map<String, ModInfo> availableMods = new HashMap<>();
    private final List<ModInfo> loadOrder = new LinkedList<>();

    public ModLoaderService(JavaPlugin plugin) {
        this.plugin = plugin;
        this.itemRegistry = new CustomItemRegistry(plugin);
        this.mobRegistry = new CustomMobRegistry(plugin);
        this.blockRegistry = new CustomBlockRegistry(plugin);
        this.commandRegistry = new CustomCommandRegistry(plugin);
        this.eventListenerRegistry = new CustomEventListenerRegistry(plugin);
        this.recipeRegistry = new CustomRecipeRegistry(plugin);
        this.worldGeneratorRegistry = new CustomWorldGeneratorRegistry(plugin);
        this.modAPI = new ModAPIImpl(plugin, itemRegistry, mobRegistry, blockRegistry, commandRegistry, eventListenerRegistry, recipeRegistry, worldGeneratorRegistry);
        this.resourceStagingDir = new File(plugin.getDataFolder(), "resource-pack-staging");
    }

    public void loadModsAndGeneratePack() {
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
            try {
                scanModloader999(modFile);
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to scan mod: " + modFile.getName());
                e.printStackTrace();
            }
        }

        if (availableMods.isEmpty()) {
            plugin.getLogger().info("No valid .modloader999 mods found after scanning.");
            return;
        }

        try {
            resolveDependenciesAndLoad();
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to resolve mod dependencies or load mods: " + e.getMessage());
            e.printStackTrace();
            disableMods();
            return;
        }

        plugin.getLogger().info("Finished processing mods. Resource pack assets staged.");
    }

    private void scanModloader999(File modFile) throws Exception {
        try (JarFile jarFile = new JarFile(modFile)) {
            JarEntry modInfoEntry = jarFile.getJarEntry("modinfo.json");
            if (modInfoEntry == null) {
                plugin.getLogger().warning("Mod " + modFile.getName() + " is missing modinfo.json. Skipping.");
                return;
            }

            String modName;
            String modVersion;
            String modAuthor;
            String mainClassName;
            Map<String, String> dependencies = new HashMap<>();

            try (InputStream is = jarFile.getInputStream(modInfoEntry);
                 InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                Gson gson = new Gson();
                JsonObject modInfoJson = gson.fromJson(reader, JsonObject.class);

                modName = modInfoJson.get("name").getAsString();
                modVersion = modInfoJson.get("version").getAsString();
                modAuthor = modInfoJson.get("author").getAsString();
                mainClassName = modInfoJson.get("main").getAsString();

                if (modInfoJson.has("dependencies")) {
                    JsonObject depsObject = modInfoJson.getAsJsonObject("dependencies");
                    Type type = new com.google.gson.reflect.TypeToken<Map<String, String>>(){}.getType();
                    dependencies = gson.fromJson(depsObject, type);
                }
            }

            ModInfo modInfo = new ModInfo(modName, modVersion, modAuthor, mainClassName, dependencies, modFile);
            availableMods.put(modName, modInfo);
            plugin.getLogger().info("Scanned mod: " + modName + " v" + modVersion + " by " + modAuthor);

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

    private void resolveDependenciesAndLoad() throws Exception {
        Map<String, Set<String>> graph = new HashMap<>();
        Map<String, Integer> inDegree = new HashMap<>();

        for (ModInfo mod : availableMods.values()) {
            graph.put(mod.getName(), new HashSet<>());
            inDegree.put(mod.getName(), 0);
        }

        for (ModInfo mod : availableMods.values()) {
            for (Map.Entry<String, String> dependency : mod.getDependencies().entrySet()) {
                String dependencyName = dependency.getKey();
                String requiredVersionRange = dependency.getValue();

                if (!availableMods.containsKey(dependencyName)) {
                    throw new Exception("Mod '" + mod.getName() + "' depends on unknown mod '" + dependencyName + "'");
                }

                ModInfo dependencyMod = availableMods.get(dependencyName);
                com.github.zafarkhaja.semver.Version dependencyVersion = com.github.zafarkhaja.semver.Version.valueOf(dependencyMod.getVersion());

                if (!dependencyVersion.satisfies(requiredVersionRange)) {
                    throw new Exception("Mod '" + mod.getName() + "' requires version " + requiredVersionRange + " of mod '" + dependencyName + "', but version " + dependencyMod.getVersion() + " is present.");
                }

                graph.get(dependencyName).add(mod.getName());
                inDegree.put(mod.getName(), inDegree.get(mod.getName()) + 1);
            }
        }

        LinkedList<ModInfo> queue = new LinkedList<>();
        for (ModInfo mod : availableMods.values()) {
            if (inDegree.get(mod.getName()) == 0) {
                queue.add(mod);
            } else {
                plugin.getLogger().info("Mod '" + mod.getName() + "' has in-degree " + inDegree.get(mod.getName()) + ".");
            }
        }

        int count = 0;
        while (!queue.isEmpty()) {
            ModInfo mod = queue.removeFirst();
            loadOrder.add(mod);
            count++;

            for (String dependentModName : graph.get(mod.getName())) {
                inDegree.put(dependentModName, inDegree.get(dependentModName) - 1);
                if (inDegree.get(dependentModName) == 0) {
                    queue.add(availableMods.get(dependentModName));
                }
            }
        }

        if (count != availableMods.size()) {
            Set<String> remainingMods = new HashSet<>(availableMods.keySet());
            for (ModInfo loadedMod : loadOrder) {
                remainingMods.remove(loadedMod.getName());
            } 
            throw new Exception("Circular dependency detected among mods: " + remainingMods);
        }

        for (ModInfo modInfo : loadOrder) {
            plugin.getLogger().info("Loading mod: " + modInfo.getName() + " v" + modInfo.getVersion());
            loadModloader999(modInfo);
        }
    }


    private void loadModloader999(ModInfo modInfo) throws Exception {
        URL[] urls = {modInfo.getModFile().toURI().toURL()};
        URLClassLoader classLoader = new URLClassLoader(urls, plugin.getClass().getClassLoader());
        modInfo.setClassLoader(classLoader);

        try (JarFile jarFile = new JarFile(modInfo.getModFile())) {
            Class<?> mainClass = classLoader.loadClass(modInfo.getMainClass());
            if (ModInitializer.class.isAssignableFrom(mainClass) && !mainClass.isInterface()) {
                plugin.getLogger().info("Found ModInitializer: " + mainClass.getName() + " for mod " + modInfo.getName());
                ModInitializer modInitializer = (ModInitializer) mainClass.getDeclaredConstructor().newInstance();
                modInfo.setInitializer(modInitializer);

                modInitializer.onLoad(modAPI);
                modInitializer.onEnable();
                plugin.getLogger().info("Mod " + modInfo.getName() + " initialized and enabled.");
            } else {
                plugin.getLogger().warning("Main class " + modInfo.getMainClass() + " in mod " + modInfo.getName() + " does not implement ModInitializer. Skipping.");
            }
        }
    }

    private void extractResource(String resourcePath, InputStream inputStream) {
        File outputFile = new File(resourceStagingDir, resourcePath);
        outputFile.getParentFile().mkdirs();

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
        Collections.reverse(loadOrder);
        for (ModInfo modInfo : loadOrder) {
            plugin.getLogger().info("Disabling mod: " + modInfo.getName());
            try {
                if (modInfo.getInitializer() != null) {
                    modInfo.getInitializer().onDisable();
                }
                if (modInfo.getClassLoader() != null) {
                    modInfo.getClassLoader().close();
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Error disabling mod " + modInfo.getName());
                e.printStackTrace();
            }
        }
        loadOrder.clear();
        availableMods.clear();
        commandRegistry.unregisterAll();
        eventListenerRegistry.unregisterAll();
        recipeRegistry.unregisterAll();
        ((com.example.modloader.api.CustomEnchantmentAPIImpl) modAPI.getCustomEnchantmentAPI()).unregisterAll();
        ((com.example.modloader.api.CustomPotionEffectAPIImpl) modAPI.getCustomPotionEffectAPI()).unregisterAll();
        ((com.example.modloader.api.CustomWorldGeneratorAPIImpl) modAPI.getCustomWorldGeneratorAPI()).unregisterAll();
    }

    public ModAPI getModAPI() {
        return modAPI;
    }

    public List<ModInfo> getLoadedModsInfo() {
        return Collections.unmodifiableList(loadOrder);
    }

    public List<String> getAvailableModNames() {
        return new ArrayList<>(availableMods.keySet());
    }

    public List<String> getEnabledModNames() {
        return loadOrder.stream().map(ModInfo::getName).collect(Collectors.toList());
    }

    public boolean isModEnabled(String modName) {
        return loadOrder.stream().anyMatch(mod -> mod.getName().equals(modName));
    }

    public ModInfo getModInfo(String modName) {
        return availableMods.get(modName);
    }

    public void unloadMod(String modName) throws Exception {
        ModInfo modToUnload = null;
        for (ModInfo mod : loadOrder) {
            if (mod.getName().equals(modName)) {
                modToUnload = mod;
                break;
            }
        }

        if (modToUnload == null) {
            throw new IllegalArgumentException("Mod '" + modName + "' is not currently loaded.");
        }

        for (ModInfo mod : loadOrder) {
            if (mod.getDependencies().keySet().contains(modName)) {
                throw new IllegalStateException("Mod '" + mod.getName() + "' depends on '" + modName + "'. Cannot unload.");
            }
        }

        try {
            plugin.getLogger().info("Unloading mod: " + modToUnload.getName());
            if (modToUnload.getInitializer() != null) {
                modToUnload.getInitializer().onDisable();
            }
            if (modToUnload.getClassLoader() != null) {
                modToUnload.getClassLoader().close();
            }
            loadOrder.remove(modToUnload);
            plugin.getLogger().info("Mod '" + modName + "' unloaded successfully!");
        } catch (Exception e) {
            throw e;
        }
    }

    public void loadMod(String modName) throws Exception {
        ModInfo modToLoad = availableMods.get(modName);
        if (modToLoad == null) {
            throw new IllegalArgumentException("Mod '" + modName + "' not found.");
        }
        if (isModEnabled(modName)) {
            throw new IllegalStateException("Mod '" + modName + "' is already loaded.");
        }

        loadOrder.add(modToLoad);

        try {
            loadModloader999(modToLoad);
            plugin.getLogger().info("Mod '" + modName + "' loaded successfully!");
        } catch (Exception e) {
            loadOrder.remove(modToLoad);
            throw e;
        }
    }

    public void enableMod(String modName) throws Exception {
        ModInfo modToEnable = availableMods.get(modName);
        if (modToEnable == null) {
            throw new IllegalArgumentException("Mod '" + modName + "' not found.");
        }
        if (isModEnabled(modName)) {
            throw new IllegalStateException("Mod '" + modName + "' is already enabled.");
        }

        loadOrder.add(modToEnable);

        try {
            loadModloader999(modToEnable);
            plugin.getLogger().info("Mod '" + modName + "' enabled successfully!");
        } catch (Exception e) {
            loadOrder.remove(modToEnable);
            throw e;
        }
    }

    public void disableMod(String modName) throws Exception {
        ModInfo modToDisable = null;
        for (ModInfo mod : loadOrder) {
            if (mod.getName().equals(modName)) {
                modToDisable = mod;
                break;
            }
        }

        if (modToDisable == null) {
            throw new IllegalArgumentException("Mod '" + modName + "' is not currently enabled.");
        }

        for (ModInfo mod : loadOrder) {
            if (mod.getDependencies().keySet().contains(modName)) {
                throw new IllegalStateException("Mod '" + mod.getName() + "' depends on '" + modName + "'. Cannot disable.");
            }
        }

        try {
            plugin.getLogger().info("Disabling mod: " + modToDisable.getName());
            if (modToDisable.getInitializer() != null) {
                modToDisable.getInitializer().onDisable();
            }
            if (modToDisable.getClassLoader() != null) {
                modToDisable.getClassLoader().close();
            }
            loadOrder.remove(modToDisable);
            plugin.getLogger().info("Mod '" + modName + "' disabled successfully.");
        } catch (Exception e) {
            throw e;
        }
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
