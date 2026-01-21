package com.example.modloader;

import org.bukkit.plugin.java.JavaPlugin;
import java.io.*;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ResourcePackGenerator {
    private final JavaPlugin plugin;
    private final AssetManager assetManager;
    private File zipFile;

    public ResourcePackGenerator(JavaPlugin plugin, AssetManager assetManager) {
        this.plugin = plugin;
        this.assetManager = assetManager;
    }

    public boolean generate() {
        File outputDir = new File(plugin.getDataFolder(), "generated");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        zipFile = new File(outputDir, "resources.zip");

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            // Add pack.mcmeta
            addPackMcmeta(zos);

            // Add all staged assets
            Map<String, File> assets = assetManager.getAllStagedAssets();
            File stagingDir = assetManager.getResourcePackStagingDir();

            for (Map.Entry<String, File> entry : assets.entrySet()) {
                File assetFile = entry.getValue();
                if (assetFile.exists()) {
                    String relativePath = getRelativePath(stagingDir, assetFile);
                    addFileToZip(zos, assetFile, relativePath);
                }
            }

            // Add assets directory if exists
            File assetsDir = new File(stagingDir, "assets");
            if (assetsDir.exists() && assetsDir.isDirectory()) {
                addDirectoryToZip(zos, assetsDir, "assets");
            }

            plugin.getLogger().info("Generated resource pack: " + zipFile.getAbsolutePath());
            return true;
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to generate resource pack: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void addPackMcmeta(ZipOutputStream zos) throws IOException {
        String packMcmeta = "{\n" +
            "  \"pack\": {\n" +
            "    \"pack_format\": 15,\n" +
            "    \"description\": \"ModLoader999 Generated Resource Pack\"\n" +
            "  }\n" +
            "}";

        ZipEntry entry = new ZipEntry("pack.mcmeta");
        zos.putNextEntry(entry);
        zos.write(packMcmeta.getBytes());
        zos.closeEntry();
    }

    private void addFileToZip(ZipOutputStream zos, File file, String entryPath) throws IOException {
        ZipEntry entry = new ZipEntry(entryPath);
        zos.putNextEntry(entry);

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                zos.write(buffer, 0, bytesRead);
            }
        }

        zos.closeEntry();
    }

    private void addDirectoryToZip(ZipOutputStream zos, File directory, String basePath) throws IOException {
        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            String entryPath = basePath + "/" + file.getName();
            if (file.isDirectory()) {
                addDirectoryToZip(zos, file, entryPath);
            } else {
                addFileToZip(zos, file, entryPath);
            }
        }
    }

    private String getRelativePath(File base, File file) {
        String basePath = base.getAbsolutePath();
        String filePath = file.getAbsolutePath();
        
        if (filePath.startsWith(basePath)) {
            String relative = filePath.substring(basePath.length());
            if (relative.startsWith(File.separator)) {
                relative = relative.substring(1);
            }
            return relative.replace(File.separator, "/");
        }
        
        return file.getName();
    }

    public File getZipFile() {
        return zipFile;
    }
}
