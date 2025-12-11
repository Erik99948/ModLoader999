package com.example.modloader.api;

import org.bukkit.plugin.java.JavaPlugin;
import com.example.modloader.AssetManager;

import java.io.File;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

public class CustomAssetAPIImpl implements CustomAssetAPI {

    private final JavaPlugin plugin;
    private final String modId;
    private final URLClassLoader modClassLoader;
    private final AssetManager assetManager;
    private final Map<String, AssetBundle> registeredBundles = new HashMap<>();

    public CustomAssetAPIImpl(JavaPlugin plugin, String modId, URLClassLoader modClassLoader, AssetManager assetManager) {
        this.plugin = plugin;
        this.modId = modId;
        this.modClassLoader = modClassLoader;
        this.assetManager = assetManager;
    }

    @Override
    public void registerSound(String assetId, String soundFilePath) {
        registerSound(assetId, soundFilePath, 0);
    }

    @Override
    public void registerSound(String assetId, String soundFilePath, int priority) {
        assetManager.registerAsset(modId, assetId, soundFilePath, modClassLoader, priority);
    }

    @Override
    public void registerModel(String assetId, String modelFilePath) {
        registerModel(assetId, modelFilePath, 0);
    }

    @Override
    public void registerModel(String assetId, String modelFilePath, int priority) {
        assetManager.registerAsset(modId, assetId, modelFilePath, modClassLoader, priority);
    }

    @Override
    public void registerTexture(String assetId, String textureFilePath) {
        registerTexture(assetId, textureFilePath, 0);
    }

    @Override
    public void registerTexture(String assetId, String textureFilePath, int priority) {
        assetManager.registerAsset(modId, assetId, textureFilePath, modClassLoader, priority);
    }

    @Override
    public File getAssetFile(String assetId) {
        return assetManager.getAllStagedAssets().get(assetId);
    }

    @Override
    public String getAssetUrl(String assetId) {
        File assetFile = getAssetFile(assetId);
        if (assetFile != null) {
            String absolutePath = assetFile.getAbsolutePath();
            String stagingDirPath = assetManager.getResourcePackStagingDir().getAbsolutePath();
            if (absolutePath.startsWith(stagingDirPath)) {
                String relativePath = absolutePath.substring(stagingDirPath.length());
                if (relativePath.startsWith(File.separator)) {
                    relativePath = relativePath.substring(File.separator.length());
                }
                return relativePath.replace(File.separator, "/");
            }
        }
        return null;
    }

    @Override
    public AssetBundle createAssetBundle(String bundleId) {
        return new AssetBundleImpl(bundleId);
    }

    @Override
    public boolean registerAssetBundle(AssetBundle bundle) {
        if (registeredBundles.containsKey(bundle.getId())) {
            plugin.getLogger().warning("Asset bundle with ID '" + bundle.getId() + "' already registered. Skipping.");
            return false;
        }
        registeredBundles.put(bundle.getId(), bundle);
        plugin.getLogger().info("Registered asset bundle: " + bundle.getId());
        return true;
    }

    @Override
    public AssetBundle getAssetBundle(String bundleId) {
        return registeredBundles.get(bundleId);
    }
}
