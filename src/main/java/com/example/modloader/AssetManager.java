package com.example.modloader;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class AssetManager {

    private final JavaPlugin plugin;
    private final File resourcePackStagingDir;

    private final Map<String, StagedAssetInfo> stagedAssets = new HashMap<>();

    public AssetManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.resourcePackStagingDir = new File(plugin.getDataFolder(), "resource-pack-staging");
        if (!this.resourcePackStagingDir.exists()) {
            this.resourcePackStagingDir.mkdirs();
        }
    }

    public void registerAsset(String modId, String assetId, String filePathInJar, String assetTypeFolder, URLClassLoader modClassLoader) {
        if (stagedAssets.containsKey(assetId)) {
            plugin.getLogger().warning("Mod " + modId + ": Asset with ID '" + assetId + "' already registered. Skipping.");
            return;
        }

        File modAssetDir = new File(resourcePackStagingDir, "assets" + File.separator + modId);
        if (!modAssetDir.exists()) {
            modAssetDir.mkdirs();
        }

        File targetFile = new File(modAssetDir, assetTypeFolder + File.separator + filePathInJar);
        targetFile.getParentFile().mkdirs();

        try (InputStream is = modClassLoader.getResourceAsStream(filePathInJar)) {
            if (is == null) {
                plugin.getLogger().warning("Mod " + modId + ": Asset file '" + filePathInJar + "' not found in mod JAR. Cannot register asset '" + assetId + "'.");
                return;
            }
            try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }
            stagedAssets.put(assetId, new StagedAssetInfo(modId, assetId, targetFile));
            plugin.getLogger().info("Mod " + modId + ": Registered asset '" + assetId + "' from '" + filePathInJar + "'. Staged to: " + targetFile.getAbsolutePath());
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Mod " + modId + ": Failed to stage asset '" + assetId + "' from '" + filePathInJar + "'.", e);
        }
    }

    public Map<String, File> getAllStagedAssets() {
        Map<String, File> assets = new HashMap<>();
        stagedAssets.forEach((assetId, info) -> assets.put(assetId, info.stagedFile));
        return assets;
    }

    public void unregisterAllAssetsForMod(String modIdToUnregister) {
        stagedAssets.entrySet().removeIf(entry -> entry.getValue().modId.equals(modIdToUnregister));

        File modAssetDir = new File(resourcePackStagingDir, "assets" + File.separator + modIdToUnregister);
        if (modAssetDir.exists()) {
            deleteDirectory(modAssetDir);
            plugin.getLogger().info("Cleaned up asset staging directory for mod: " + modIdToUnregister);
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

    private static class StagedAssetInfo {
        final String modId;
        final String assetId;
        final File stagedFile;

        StagedAssetInfo(String modId, String assetId, File stagedFile) {
            this.modId = modId;
            this.assetId = assetId;
            this.stagedFile = stagedFile;
        }
    }
}