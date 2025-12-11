package com.example.modloader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URLClassLoader;

import java.util.ArrayList;
import java.util.List;

public class ModRepository {
    private final File repositoryFile;
    private final JavaPlugin plugin;
    private List<ModInfo> mods;

    public ModRepository(JavaPlugin plugin) {
        this.plugin = plugin;
        this.repositoryFile = new File(plugin.getDataFolder(), "repository.json");
        load();
    }

    private void load() {
        if (!repositoryFile.exists()) {
            mods = new ArrayList<>();
            save();
            return;
        }
        try (FileReader reader = new FileReader(repositoryFile)) {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(File.class, new FileAdapter())
                    .registerTypeAdapter(URLClassLoader.class, new URLClassLoaderAdapter())
                    .create();
            Type type = new TypeToken<List<ModInfo>>() {}.getType();
            mods = gson.fromJson(reader, type);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not load mod repository: " + e.getMessage());
            mods = new ArrayList<>();
        }
    }

    private void save() {
        try (FileWriter writer = new FileWriter(repositoryFile)) {
            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .registerTypeAdapter(File.class, new FileAdapter())
                    .registerTypeAdapter(URLClassLoader.class, new URLClassLoaderAdapter())
                    .create();
            gson.toJson(mods, writer);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save mod repository: " + e.getMessage());
        }
    }

    public boolean isModInRepository(String modName) {
        return mods.stream().anyMatch(mod -> mod.getName().equalsIgnoreCase(modName));
    }

    public void addMod(ModInfo modInfo) {
        if (!isModInRepository(modInfo.getName())) {
            mods.add(modInfo);
            save();
        }
    }

    public List<ModInfo> getAllMods() {
        return mods;
    }
}

