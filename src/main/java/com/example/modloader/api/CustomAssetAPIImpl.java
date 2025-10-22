package com.example.modloader.api;

import org.bukkit.plugin.java.JavaPlugin;
import com.example.modloader.AssetManager;

import java.io.File;
import java.net.URLClassLoader;

public class CustomAssetAPIImpl implements CustomAssetAPI {

    private final JavaPlugin plugin;
    private final String modId;
    private final URLClassLoader modClassLoader;
    private final AssetManager assetManager;

    public CustomAssetAPIImpl(JavaPlugin plugin, String modId, URLClassLoader modClassLoader, AssetManager assetManager) {
        this.plugin = plugin;
        this.modId = modId;
        this.modClassLoader = modClassLoader;
        this.assetManager = assetManager;
    }

    @Override
    public void registerSound(String assetId, String soundFilePath) {
        assetManager.registerAsset(modId, assetId, soundFilePath, "sounds", modClassLoader);
    }

    @Override
    public void registerModel(String assetId, String modelFilePath) {
        assetManager.registerAsset(modId, assetId, modelFilePath, "models", modClassLoader);
    }

    @Override
    public void registerTexture(String assetId, String textureFilePath) {
        assetManager.registerAsset(modId, assetId, textureFilePath, "textures", modClassLoader);
    }

    @Override
    public File getAssetFile(String assetId) {
        return assetManager.getAllStagedAssets().get(assetId);
    }
}