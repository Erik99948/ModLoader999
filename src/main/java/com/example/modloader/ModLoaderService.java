package com.example.modloader;

import com.example.modloader.api.ModAPI;
import com.example.modloader.api.ModAPIImpl;
import com.example.modloader.api.ModInitializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;

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

import com.example.modloader.api.event.EventBus;
import com.example.modloader.api.dependencyinjection.API;
import com.example.modloader.api.dependencyinjection.Binder;
import com.example.modloader.api.dependencyinjection.ModAPIRegistry;
import com.example.modloader.api.dependencyinjection.ModInjector;

public class ModLoaderService {

    private final JavaPlugin plugin;
    private final CustomItemRegistry itemRegistry;
    private final CustomMobRegistry mobRegistry;
    private final CustomBlockRegistry blockRegistry;
    private final CustomCommandRegistry commandRegistry;
    private final CustomEventListenerRegistry eventListenerRegistry;
    private final CustomRecipeRegistry recipeRegistry;
    private final CustomWorldGeneratorRegistry worldGeneratorRegistry;
    private final com.example.modloader.api.CustomEnchantmentAPIImpl customEnchantmentAPIImpl;
    private final com.example.modloader.api.CustomPotionEffectAPIImpl customPotionEffectAPIImpl;
    private final com.example.modloader.api.CustomWorldGeneratorAPIImpl customWorldGeneratorAPIImpl;
    private final com.example.modloader.api.ModMessageAPIImpl modMessageAPIImpl;
    private final File resourceStagingDir;
    private final ModConfigManager modConfigManager;
    private final AssetManager assetManager;
    private final ResourcePackGenerator resourcePackGenerator;
    private org.bukkit.scheduler.BukkitTask messageDispatchTask;
    private final EventBus eventBus;
    private final ModAPIRegistry modAPIRegistry;
    private final ModRepository modRepository;
    private final Map<String, ModInfo> availableMods = new HashMap<>();
    private final List<ModInfo> loadOrder = new ArrayList<>();
    private final com.example.modloader.api.network.Networking networking;

    public ModLoaderService(JavaPlugin plugin) {
        this.plugin = plugin;
        this.eventBus = new EventBus();

        this.itemRegistry = new CustomItemRegistry(plugin, eventBus);
        this.mobRegistry = new CustomMobRegistry(plugin, eventBus);
        this.blockRegistry = new CustomBlockRegistry(plugin, eventBus);
        this.commandRegistry = new CustomCommandRegistry(plugin);
        this.eventListenerRegistry = new CustomEventListenerRegistry(plugin, eventBus);
        this.recipeRegistry = new CustomRecipeRegistry(plugin);
        this.worldGeneratorRegistry = new CustomWorldGeneratorRegistry(plugin);
        this.customEnchantmentAPIImpl = new com.example.modloader.api.CustomEnchantmentAPIImpl(plugin);
        this.customPotionEffectAPIImpl = new com.example.modloader.api.CustomPotionEffectAPIImpl(plugin);
        this.customWorldGeneratorAPIImpl = new com.example.modloader.api.CustomWorldGeneratorAPIImpl(plugin);
        this.networking = new com.example.modloader.api.network.Networking(plugin);
        this.networking.registerChannel("BungeeCord");
        this.modMessageAPIImpl = new com.example.modloader.api.ModMessageAPIImpl(plugin, "ModLoaderService", this.networking);
        this.modConfigManager = new ModConfigManager(plugin);
        this.assetManager = new AssetManager(plugin);
        this.resourceStagingDir = new File(plugin.getDataFolder(), "resource-pack-staging");


        int packFormat = plugin.getConfig().getInt("resource-pack.format", 34);
        String packDescription = plugin.getConfig().getString("resource-pack.description", "Dynamically generated server resources.");
        this.resourcePackGenerator = new ResourcePackGenerator(plugin, assetManager, packFormat, packDescription);
        this.modAPIRegistry = new ModAPIRegistry();
        this.modRepository = new ModRepository(plugin);
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

            String modId;
            String modName;
            String modVersion;
            String modAuthor;
            String modDescription;
            String mainClassName;
            Map<String, String> dependencies = new HashMap<>();
            List<String> softDependencies = new ArrayList<>();
            Map<String, String> customProperties = new HashMap<>();


            String apiVersion = "1.0.0";


            try (InputStream is = jarFile.getInputStream(modInfoEntry);
                 InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode modInfoJson = objectMapper.readTree(reader);

                modId = modInfoJson.get("id").asText();
                modName = modInfoJson.get("name").asText();
                modVersion = modInfoJson.get("version").asText();
                modAuthor = modInfoJson.get("author").asText();
                modDescription = modInfoJson.has("description") ? modInfoJson.get("description").asText() : "";
                mainClassName = modInfoJson.get("main").asText();


                if (modInfoJson.has("dependencies")) {
                    dependencies = objectMapper.convertValue(modInfoJson.get("dependencies"), new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {});
                }

                if (modInfoJson.has("softDependencies")) {
                    for (JsonNode softDepNode : modInfoJson.get("softDependencies")) {
                        softDependencies.add(softDepNode.asText());
                    }
                }

                if (modInfoJson.has("customProperties")) {
                    customProperties = objectMapper.convertValue(modInfoJson.get("customProperties"), new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {});
                }
            }

            ModInfo modInfo = new ModInfo(modId, modName, modVersion, modAuthor, modDescription, mainClassName, dependencies, softDependencies, customProperties, modFile, apiVersion);
            modInfo.setState(ModState.LOADED);
            availableMods.put(modName, modInfo);
            plugin.getLogger().info("Scanned mod: " + modName + " v" + modVersion + " by " + modAuthor);

            if (!modRepository.isModInRepository(modName)) {
                plugin.getLogger().info("Mod '" + modName + "' is not in the public repository. To publish it, use the command: /modloader publishmod " + modName);
            }

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
            URL[] urls = {mod.getModFile().toURI().toURL()};
            URLClassLoader classLoader = new URLClassLoader(urls, plugin.getClass().getClass().getClassLoader());
            mod.setClassLoader(classLoader);
        }

        for (ModInfo mod : availableMods.values()) {
            if (mod.getState() == ModState.ERRORED) {
                continue;
            }


            for (Map.Entry<String, String> dependency : mod.getDependencies().entrySet()) {
                String dependencyName = dependency.getKey();
                String requiredVersionRange = dependency.getValue();

                if (!availableMods.containsKey(dependencyName)) {
                    plugin.getLogger().severe("Mod '" + mod.getName() + "' depends on unknown mod '" + dependencyName + "'. Marking as ERRORED.");
                    mod.setState(ModState.ERRORED);
                    break;
                }

                ModInfo dependencyMod = availableMods.get(dependencyName);
                com.github.zafarkhaja.semver.Version dependencyVersion = com.github.zafarkhaja.semver.Version.valueOf(dependencyMod.getVersion());

                if (!dependencyVersion.satisfies(requiredVersionRange)) {
                    plugin.getLogger().severe("Mod '" + mod.getName() + "' requires version " + requiredVersionRange + " of mod '" + dependencyName + "', but version " + dependencyMod.getVersion() + " is present. Marking as ERRORED.");
                    mod.setState(ModState.ERRORED);
                    break;
                }

                try {
                    Class<?> dependencyMainClass = dependencyMod.getClassLoader().loadClass(dependencyMod.getMainClass());


                } catch (ClassNotFoundException e) {
                    plugin.getLogger().severe("Could not find main class for dependency '" + dependencyName + "'. Marking as ERRORED.");
                    mod.setState(ModState.ERRORED);
                    break;
                }

                if (mod.getState() != ModState.ERRORED) {
                    graph.get(dependencyName).add(mod.getName());
                    inDegree.put(mod.getName(), inDegree.get(mod.getName()) + 1);
                }
            }


            if (mod.getState() != ModState.ERRORED) {
                for (String softDependencyName : mod.getSoftDependencies()) {
                    if (availableMods.containsKey(softDependencyName)) {

                        graph.get(softDependencyName).add(mod.getName());

                    } else {
                        plugin.getLogger().info("Mod '" + mod.getName() + "' has a soft dependency on unknown mod '" + softDependencyName + "'. Continuing without it.");
                    }
                }
            }
        }

        LinkedList<ModInfo> queue = new LinkedList<>();
        for (ModInfo mod : availableMods.values()) {
            if (mod.getState() != ModState.ERRORED && inDegree.get(mod.getName()) == 0) {
                queue.add(mod);
            } else if (mod.getState() != ModState.ERRORED) {
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
            if (modInfo.getState() == ModState.ERRORED) {
                plugin.getLogger().warning("Skipping errored mod: " + modInfo.getName());
                continue;
            }

            plugin.getLogger().info("Loading mod: " + modInfo.getName() + " v" + modInfo.getVersion());


            URL[] urls = {modInfo.getModFile().toURI().toURL()};
            URLClassLoader classLoader = new URLClassLoader(urls, plugin.getClass().getClassLoader());
            modInfo.setClassLoader(classLoader);

            try (JarFile jarFile = new JarFile(modInfo.getModFile())) {
                for (JarEntry entry : Collections.list(jarFile.entries())) {
                    if (entry.getName().endsWith(".class")) {
                        String className = entry.getName().replace('/', '.').substring(0, entry.getName().length() - 6);
                        Class<?> clazz = classLoader.loadClass(className);
                        if (clazz.isAnnotationPresent(API.class)) {
                            plugin.getLogger().info("Found API class: " + clazz.getName() + " in mod " + modInfo.getName());
                            modAPIRegistry.registerAPI((Class<Object>) clazz, clazz.getDeclaredConstructor().newInstance());
                        }
                    }
                }
            }

            Class<?> mainClass = classLoader.loadClass(modInfo.getMainClass());
            if (ModInitializer.class.isAssignableFrom(mainClass) && !mainClass.isInterface()) {
                plugin.getLogger().info("Found ModInitializer: " + mainClass.getName() + " for mod " + modInfo.getName());
                Binder binder = new Binder();
                // ModInjector injector = new ModInjector(binder, modAPIRegistry); // Not directly used here
                ModInitializer modInitializer = (ModInitializer) mainClass.getDeclaredConstructor().newInstance();
                modInitializer.configure(binder);
                binder.registerProviders(modInitializer);
                modInfo.setInitializer(modInitializer);
                modInfo.setState(ModState.LOADED);

                try {
                    eventBus.post(new com.example.modloader.api.event.ModPreLoadEvent(modInfo));
                    modInfo.setState(ModState.INITIALIZING);
                    modConfigManager.loadModConfig(modInfo);
                    ModAPI modAPI = new ModAPIImpl(plugin, itemRegistry, mobRegistry, blockRegistry, commandRegistry, eventListenerRegistry, recipeRegistry, worldGeneratorRegistry, customEnchantmentAPIImpl, customPotionEffectAPIImpl, customWorldGeneratorAPIImpl, modConfigManager, modMessageAPIImpl, assetManager, modInfo.getName(), modInfo.getClassLoader(), eventBus);
                    modInfo.getInitializer().onPreLoad(modAPI);
                    modInfo.getInitializer().onLoad(modAPI);
                    modInfo.getInitializer().onPostLoad(modAPI);
                    plugin.getLogger().info("Mod " + modInfo.getName() + " initialized.");
                    eventBus.post(new com.example.modloader.api.event.ModPostLoadEvent(modInfo));

                    eventBus.post(new com.example.modloader.api.event.ModPreEnableEvent(modInfo));
                    modInfo.setState(ModState.ENABLED);
                    modInfo.getInitializer().onEnable();
                    plugin.getLogger().info("Mod " + modInfo.getName() + " enabled.");
                    eventBus.post(new com.example.modloader.api.event.ModPostEnableEvent(modInfo));

                    if (!loadOrder.contains(modInfo)) {
                        loadOrder.add(modInfo);
                    }
                } catch (Throwable e) {
                    plugin.getLogger().severe("Failed to load or enable mod " + modInfo.getName() + ": " + e.getMessage());
                    e.printStackTrace();
                    modInfo.setState(ModState.ERRORED);
                } finally {
                }
            } else {
                plugin.getLogger().warning("Main class " + modInfo.getMainClass() + " in mod " + modInfo.getName() + " does not implement ModInitializer. Marking as ERRORED.");
                modInfo.setState(ModState.ERRORED);
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
        List<ModInfo> modsToDisable = new ArrayList<>(loadOrder);
        Collections.reverse(modsToDisable);

        for (ModInfo modInfo : modsToDisable) {
            if (modInfo.getState() == ModState.ENABLED || modInfo.getState() == ModState.ERRORED) {
                plugin.getLogger().info("Disabling mod: " + modInfo.getName());
                eventBus.post(new com.example.modloader.api.event.ModPreDisableEvent(modInfo));
                modInfo.setState(ModState.DISABLING);
                try {
                    if (modInfo.getInitializer() != null) {
                        modInfo.getInitializer().onPreDisable();
                        modInfo.getInitializer().onDisable();
                        modInfo.getInitializer().onPostDisable();
                    }
                    modConfigManager.unloadModConfig(modInfo.getName());
                    commandRegistry.unregisterAll(modInfo.getName());
                    eventListenerRegistry.unregisterAll(modInfo.getName());
                    modMessageAPIImpl.unregisterAllHandlersForMod(modInfo.getName());
                    assetManager.unregisterAllAssetsForMod(modInfo.getName());
                    if (modInfo.getClassLoader() != null) {
                        modInfo.getClassLoader().close();
                    }
                    modInfo.setState(ModState.DISABLED);
                    eventBus.post(new com.example.modloader.api.event.ModPostDisableEvent(modInfo));
                } catch (Exception e) {
                    plugin.getLogger().severe("Error disabling mod " + modInfo.getName() + ": " + e.getMessage());
                    e.printStackTrace();
                    modInfo.setState(ModState.ERRORED);
                } finally {
                }
            }
        }
        loadOrder.clear();
        availableMods.clear();
        commandRegistry.unregisterAll();
        eventListenerRegistry.unregisterAll();
        recipeRegistry.unregisterAll();
        customEnchantmentAPIImpl.unregisterAll();
        customPotionEffectAPIImpl.unregisterAll();
        customWorldGeneratorAPIImpl.unregisterAll();

        if (messageDispatchTask != null) {
            messageDispatchTask.cancel();
            plugin.getLogger().info("Message dispatch task cancelled.");
        }
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

    public ResourcePackGenerator getResourcePackGenerator() {
        return resourcePackGenerator;
    }

    public ModConfigManager getModConfigManager() {
        return modConfigManager;
    }

    public ModRepository getModRepository() {
        return modRepository;
    }

    public com.example.modloader.gui.ModManagementGUI createModManagementGUI() {
        return new com.example.modloader.gui.ModManagementGUI(plugin, this);
    }

    public void unloadMod(String modName) throws Exception {
        ModInfo modToUnload = getModInfo(modName);
        if (modToUnload == null) {
            throw new IllegalArgumentException("Mod '" + modName + "' not found.");
        }

        if (modToUnload.getState() != ModState.ENABLED && modToUnload.getState() != ModState.ERRORED) {
            throw new IllegalStateException("Mod '" + modName + "' is not currently enabled or errored. Current state: " + modToUnload.getState());
        }

        for (ModInfo mod : loadOrder) {
            if (mod.getState() == ModState.ENABLED && mod.getDependencies().containsKey(modName)) {
                throw new IllegalStateException("Mod '" + mod.getName() + "' depends on '" + modName + "'. Cannot unload '" + modName + "' while '" + mod.getName() + "' is enabled.");
            }
        }

        try {
            plugin.getLogger().info("Unloading mod: " + modToUnload.getName());
            modToUnload.setState(ModState.DISABLING);
            if (modToUnload.getInitializer() != null) {
                modToUnload.getInitializer().onDisable();
            }
            modConfigManager.unloadModConfig(modToUnload.getName());
            commandRegistry.unregisterAll(modToUnload.getName());
            eventListenerRegistry.unregisterAll(modToUnload.getName());
            modMessageAPIImpl.unregisterAllHandlersForMod(modToUnload.getName());
            assetManager.unregisterAllAssetsForMod(modToUnload.getName());
            if (modToUnload.getClassLoader() != null) {
                modToUnload.getClassLoader().close();
            }
            loadOrder.remove(modToUnload);
            availableMods.remove(modName);
            modToUnload.setState(ModState.UNLOADED);
            plugin.getLogger().info("Mod '" + modName + "' unloaded successfully!");
        } catch (Exception e) {
            modToUnload.setState(ModState.ERRORED);
            plugin.getLogger().severe("Error unloading mod " + modToUnload.getName() + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public void loadMod(String modName) throws Exception {
        ModInfo modToLoad = availableMods.get(modName);
        if (modToLoad == null) {
            throw new IllegalArgumentException("Mod '" + modName + "' not found.");
        }

        if (modToLoad.getState() == ModState.LOADED || modToLoad.getState() == ModState.INITIALIZING || modToLoad.getState() == ModState.ENABLED) {
            throw new IllegalStateException("Mod '" + modName + "' is already loaded or enabled. Current state: " + modToLoad.getState());
        }
        if (modToLoad.getState() == ModState.ERRORED) {
            throw new IllegalStateException("Mod '" + modName + "' is in an ERRORED state and cannot be loaded.");
        }

        if (modToLoad.getState() == ModState.DISABLED) {
            modToLoad.setState(ModState.LOADED);
            plugin.getLogger().info("Mod '" + modName + "' state changed from DISABLED to LOADED.");
            return;
        }

        try {
            URL[] urls = {modToLoad.getModFile().toURI().toURL()};
            URLClassLoader classLoader = new URLClassLoader(urls, plugin.getClass().getClassLoader());
            modToLoad.setClassLoader(classLoader);

            try (JarFile jarFile = new JarFile(modToLoad.getModFile())) {
                for (JarEntry entry : Collections.list(jarFile.entries())) {
                    if (entry.getName().endsWith(".class")) {
                        String className = entry.getName().replace('/', '.').substring(0, entry.getName().length() - 6);
                        Class<?> clazz = classLoader.loadClass(className);
                        if (clazz.isAnnotationPresent(API.class)) {
                            plugin.getLogger().info("Found API class: " + clazz.getName() + " in mod " + modToLoad.getName());
                            modAPIRegistry.registerAPI((Class<Object>) clazz, clazz.getDeclaredConstructor().newInstance());
                        }
                    }
                }
            }
            plugin.getLogger().info("Mod '" + modToLoad.getName() + "' classes loaded successfully!");

            Class<?> mainClass = classLoader.loadClass(modToLoad.getMainClass());
            if (ModInitializer.class.isAssignableFrom(mainClass) && !mainClass.isInterface()) {
                plugin.getLogger().info("Found ModInitializer: " + mainClass.getName() + " for mod " + modToLoad.getName());
                Binder binder = new Binder(); // Re-initialize binder for each mod
                ModInjector injector = new ModInjector(binder, modAPIRegistry);
                ModInitializer modInitializer = (ModInitializer) mainClass.getDeclaredConstructor().newInstance();
                modInitializer.configure(binder);
                binder.registerProviders(modInitializer);
                modToLoad.setInitializer(modInitializer);
                modToLoad.setState(ModState.LOADED);


                try {
                    eventBus.post(new com.example.modloader.api.event.ModPreLoadEvent(modToLoad));
                    modToLoad.setState(ModState.INITIALIZING);
                    modConfigManager.loadModConfig(modToLoad);
                    ModAPI modAPI = new ModAPIImpl(plugin, itemRegistry, mobRegistry, blockRegistry, commandRegistry, eventListenerRegistry, recipeRegistry, worldGeneratorRegistry, customEnchantmentAPIImpl, customPotionEffectAPIImpl, customWorldGeneratorAPIImpl, modConfigManager, modMessageAPIImpl, assetManager, modToLoad.getName(), modToLoad.getClassLoader(), eventBus);
                    modToLoad.getInitializer().onPreLoad(modAPI);
                    modToLoad.getInitializer().onLoad(modAPI);
                    modToLoad.getInitializer().onPostLoad(modAPI);
                    plugin.getLogger().info("Mod " + modToLoad.getName() + " initialized.");
                    eventBus.post(new com.example.modloader.api.event.ModPostLoadEvent(modToLoad));

                    eventBus.post(new com.example.modloader.api.event.ModPreEnableEvent(modToLoad));
                    modToLoad.setState(ModState.ENABLED);
                    modToLoad.getInitializer().onEnable();
                    plugin.getLogger().info("Mod " + modToLoad.getName() + " enabled.");
                    eventBus.post(new com.example.modloader.api.event.ModPostEnableEvent(modToLoad));

                    if (!loadOrder.contains(modToLoad)) {
                        loadOrder.add(modToLoad);
                    }
                } catch (Throwable e) {
                    plugin.getLogger().severe("Failed to load or enable mod " + modToLoad.getName() + ": " + e.getMessage());
                    e.printStackTrace();
                    modToLoad.setState(ModState.ERRORED);
                } finally {
                }
            } else {
                plugin.getLogger().warning("Main class " + modToLoad.getMainClass() + " in mod " + modToLoad.getName() + " does not implement ModInitializer. Marking as ERRORED.");
                modToLoad.setState(ModState.ERRORED);
            }
        } catch (Exception e) {
            modToLoad.setState(ModState.ERRORED);
            plugin.getLogger().severe("Error loading classes for mod " + modToLoad.getName() + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public void enableMod(String modName) throws Exception {
        ModInfo modToEnable = availableMods.get(modName);
        if (modToEnable == null) {
            throw new IllegalArgumentException("Mod '" + modName + "' not found.");
        }

        if (modToEnable.getState() == ModState.ENABLED) {
            throw new IllegalStateException("Mod '" + modName + "' is already enabled.");
        }
        if (modToEnable.getState() == ModState.ERRORED) {
            throw new IllegalStateException("Mod '" + modName + "' is in an ERRORED state and cannot be enabled.");
        }

        try {
            recursivelyEnableMod(modToEnable, new HashSet<>());
            plugin.getLogger().info("Mod '" + modName + "' enabled successfully!");
        } catch (Exception e) {
            plugin.getLogger().severe("Error enabling mod " + modToEnable.getName() + ": " + e.getMessage());
            e.printStackTrace();
            modToEnable.setState(ModState.ERRORED);
            throw e;
        }
    }

    public void disableMod(String modName) throws Exception {
        ModInfo modToDisable = getModInfo(modName);
        if (modToDisable == null) {
            throw new IllegalArgumentException("Mod '" + modName + "' not found.");
        }

        if (modToDisable.getState() != ModState.ENABLED && modToDisable.getState() != ModState.ERRORED) {
            throw new IllegalStateException("Mod '" + modName + "' is not currently enabled or errored. Current state: " + modToDisable.getState());
        }

        for (ModInfo mod : loadOrder) {
            if (mod.getState() == ModState.ENABLED && mod.getDependencies().containsKey(modName)) {
                throw new IllegalStateException("Mod '" + mod.getName() + "' depends on '" + modName + "'. Cannot disable '" + modName + "' while '" + mod.getName() + "' is enabled.");
            }
        }

        try {
            plugin.getLogger().info("Disabling mod: " + modToDisable.getName());
            modToDisable.setState(ModState.DISABLING);
            if (modToDisable.getInitializer() != null) {
                modToDisable.getInitializer().onPreDisable();
                modToDisable.getInitializer().onDisable();
                modToDisable.getInitializer().onPostDisable();
            }
            modConfigManager.unloadModConfig(modToDisable.getName());
            commandRegistry.unregisterAll(modToDisable.getName());
            eventListenerRegistry.unregisterAll(modToDisable.getName());
            modMessageAPIImpl.unregisterAllHandlersForMod(modToDisable.getName());
            assetManager.unregisterAllAssetsForMod(modToDisable.getName());
            if (modToDisable.getClassLoader() != null) {
                modToDisable.getClassLoader().close();
            }
            loadOrder.remove(modToDisable);
            availableMods.remove(modName);
            modToDisable.setState(ModState.UNLOADED);
            plugin.getLogger().info("Mod '" + modName + "' unloaded successfully!");
        } catch (Exception e) {
            modToDisable.setState(ModState.ERRORED);
            plugin.getLogger().severe("Error unloading mod " + modToDisable.getName() + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private void recursivelyEnableMod(ModInfo modInfo, Set<String> enablingStack) throws Exception {
        if (modInfo.getState() == ModState.ENABLED) {
            return;
        }
        if (modInfo.getState() == ModState.ERRORED) {
            throw new IllegalStateException("Cannot enable mod '" + modInfo.getName() + "' because it is in an ERRORED state.");
        }

        if (enablingStack.contains(modInfo.getName())) {
            throw new IllegalStateException("Circular dependency detected involving mod '" + modInfo.getName() + "'.");
        }
        enablingStack.add(modInfo.getName());

        for (Map.Entry<String, String> dependencyEntry : modInfo.getDependencies().entrySet()) {
            String dependencyName = dependencyEntry.getKey();
            ModInfo dependencyMod = availableMods.get(dependencyName);

            if (dependencyMod == null) {
                throw new IllegalStateException("Mod '" + modInfo.getName() + "' depends on unknown mod '" + dependencyName + "'.");
            }

            if (dependencyMod.getState() != ModState.ENABLED) {
                recursivelyEnableMod(dependencyMod, enablingStack);
            }
        }

        // Handle soft dependencies during enabling
        for (String softDependencyName : modInfo.getSoftDependencies()) {
            ModInfo softDependencyMod = availableMods.get(softDependencyName);
            if (softDependencyMod == null) {
                plugin.getLogger().info("Mod '" + modInfo.getName() + "' has a soft dependency on unknown mod '" + softDependencyName + "'. Continuing without it.");
            } else if (softDependencyMod.getState() != ModState.ENABLED) {
                // Attempt to enable soft dependency if not already enabled
                try {
                    recursivelyEnableMod(softDependencyMod, enablingStack);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to enable soft dependency '" + softDependencyName + "' for mod '" + modInfo.getName() + "': " + e.getMessage());
                    // Do not re-throw, soft dependencies are optional
                }
            }
        }

        try {
            if (modInfo.getState() == ModState.UNLOADED || modInfo.getState() == ModState.DISABLED) {

                URL[] urls = {modInfo.getModFile().toURI().toURL()};
                URLClassLoader classLoader = new URLClassLoader(urls, plugin.getClass().getClassLoader());
                modInfo.setClassLoader(classLoader);

                try (JarFile jarFile = new JarFile(modInfo.getModFile())) {
                    for (JarEntry entry : Collections.list(jarFile.entries())) {
                        if (entry.getName().endsWith(".class")) {
                            String className = entry.getName().replace('/', '.').substring(0, entry.getName().length() - 6);
                            Class<?> clazz = classLoader.loadClass(className);
                            if (clazz.isAnnotationPresent(API.class)) {
                                plugin.getLogger().info("Found API class: " + clazz.getName() + " in mod " + modInfo.getName());
                                modAPIRegistry.registerAPI((Class<Object>) clazz, clazz.getDeclaredConstructor().newInstance());
                            }
                        }
                    }
                }

                Class<?> mainClass = classLoader.loadClass(modInfo.getMainClass());
                if (ModInitializer.class.isAssignableFrom(mainClass) && !mainClass.isInterface()) {
                    plugin.getLogger().info("Found ModInitializer: " + mainClass.getName() + " for mod " + modInfo.getName());
                    Binder binder = new Binder();
                    ModInjector injector = new ModInjector(binder, modAPIRegistry);
                    ModInitializer modInitializer = (ModInitializer) mainClass.getDeclaredConstructor().newInstance();
                    modInitializer.configure(binder);
                    binder.registerProviders(modInitializer);
                    modInfo.setInitializer(modInitializer);
                    modInfo.setState(ModState.LOADED);
                } else {
                    plugin.getLogger().warning("Main class " + modInfo.getMainClass() + " in mod " + modInfo.getName() + " does not implement ModInitializer. Marking as ERRORED.");
                    modInfo.setState(ModState.ERRORED);
                }
            }

            if (modInfo.getState() == ModState.LOADED) {
                modInfo.setState(ModState.INITIALIZING);
                modConfigManager.loadModConfig(modInfo);
                ModAPI modAPI = new ModAPIImpl(plugin, itemRegistry, mobRegistry, blockRegistry, commandRegistry, eventListenerRegistry, recipeRegistry, worldGeneratorRegistry, customEnchantmentAPIImpl, customPotionEffectAPIImpl, customWorldGeneratorAPIImpl, modConfigManager, modMessageAPIImpl, assetManager, modInfo.getName(), modInfo.getClassLoader(), eventBus);
                modInfo.getInitializer().onPreLoad(modAPI);
                modInfo.getInitializer().onLoad(modAPI);
                modInfo.getInitializer().onPostLoad(modAPI);
                plugin.getLogger().info("Mod " + modInfo.getName() + " initialized.");

                modInfo.setState(ModState.ENABLED);
                modInfo.getInitializer().onEnable();
                plugin.getLogger().info("Mod " + modInfo.getName() + " enabled.");

                if (!loadOrder.contains(modInfo)) {
                    loadOrder.add(modInfo);
                }
            } else {
                throw new IllegalStateException("Unexpected state for mod '" + modInfo.getName() + "' during enabling: " + modInfo.getState());
            }
        } catch (Exception e) {
            modInfo.setState(ModState.ERRORED);
            plugin.getLogger().severe("Error enabling mod " + modInfo.getName() + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            enablingStack.remove(modInfo.getName());
        }
    }

    public void hotReloadMod(String modName) throws Exception {
        ModInfo modToReload = getModInfo(modName);
        if (modToReload == null) {
            throw new IllegalArgumentException("Mod '" + modName + "' not found.");
        }

        if (modToReload.getState() != ModState.ENABLED) {
            throw new IllegalStateException("Mod '" + modName + "' is not currently enabled. Current state: " + modToReload.getState());
        }

        plugin.getLogger().info("Attempting to hot-reload mod: " + modName);


        disableMod(modName);


        loadMod(modName);


        enableMod(modName);

        plugin.getLogger().info("Mod '" + modName + "' hot-reloaded successfully!");
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