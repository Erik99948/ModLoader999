package com.example.modloader;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ResourcePackGenerator {

    private final JavaPlugin plugin;
    private final File stagingDir;
    private final File zipFile;
    private final AssetManager assetManager;
    private String zipFileSha1;

    public ResourcePackGenerator(JavaPlugin plugin, AssetManager assetManager) {
        this.plugin = plugin;
        this.assetManager = assetManager;
        this.stagingDir = new File(plugin.getDataFolder(), "resource-pack-staging");
        this.zipFile = new File(plugin.getDataFolder(), "generated-pack.zip");
    }

    public boolean generate() {
        try {
            if (!stagingDir.exists()) {
                stagingDir.mkdirs();
            }
            createPackMeta();
            addCustomAssets();

            zipDirectory(stagingDir, zipFile);

            this.zipFileSha1 = calculateSha1(zipFile);
            plugin.getLogger().info("Generated resource pack with SHA-1: " + this.zipFileSha1);

            return true;
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to generate resource pack zip.");
            e.printStackTrace();
            return false;
        }
    }

    private void addCustomAssets() throws IOException {
        plugin.getLogger().info("Adding custom assets to resource pack staging directory.");


        for (File assetFile : assetManager.getAllStagedAssets().values()) {
            File relativePath = new File(stagingDir.toURI().relativize(assetFile.toURI()).getPath());
            File targetFile = new File(stagingDir, relativePath.getPath());
            targetFile.getParentFile().mkdirs();
            try (FileInputStream in = new FileInputStream(assetFile);
                 FileOutputStream out = new FileOutputStream(targetFile)) {
                byte[] buffer = new byte[4096];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
                plugin.getLogger().info("Copied staged asset: " + assetFile.getName());
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to copy staged asset " + assetFile.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void copyFolder(File sourceFolder, File targetFolder) throws IOException {
        if (!targetFolder.exists()) {
            targetFolder.mkdirs();
        }

        for (String f : sourceFolder.list()) {
            File srcFile = new File(sourceFolder, f);
            File destFile = new File(targetFolder, f);

            if (srcFile.isDirectory()) {
                copyFolder(srcFile, destFile);
            } else {
                String fileName = srcFile.getName().toLowerCase();
                if (fileName.endsWith(".png") || fileName.endsWith(".json") || fileName.endsWith(".ogg")) {
                    try (FileInputStream in = new FileInputStream(srcFile);
                         FileOutputStream out = new FileOutputStream(destFile)) {
                        byte[] buffer = new byte[4096];
                        int length;
                        while ((length = in.read(buffer)) > 0) {
                            out.write(buffer, 0, length);
                        }
                        plugin.getLogger().info("Copied asset: " + srcFile.getName());

                        if (fileName.endsWith(".json")) {
                            validateJsonFile(destFile);
                        }
                    }
                }
            }
        }
    }

    public File getZipFile() {
        return zipFile;
    }

    public String getZipFileSha1() {
        return zipFileSha1;
    }

    private void createPackMeta() throws IOException {
        File packMetaFile = new File(stagingDir, "pack.mcmeta");
        try (FileWriter writer = new FileWriter(packMetaFile)) {
            writer.write("{\n");
            writer.write("  \"pack\": {\n");
            writer.write("    \"pack_format\": 34,\n");
            writer.write("    \"description\": \"Dynamically generated server resources.\"\n");
            writer.write("  }\n");
            writer.write("}\n");
        }
        plugin.getLogger().info("Created pack.mcmeta file.");
    }

    private void zipDirectory(File dir, File zipFile) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            addDirToZip(dir, "", zos);
        }
        plugin.getLogger().info("Successfully zipped resource pack to: " + zipFile.getPath());
    }

    private void copyResource(String resourcePath, File targetFile) throws IOException {
        try (java.io.InputStream in = plugin.getResource(resourcePath);
             FileOutputStream out = new FileOutputStream(targetFile)) {
            if (in == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }
            byte[] buffer = new byte[4096];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }
    }

    private void addDirToZip(File dir, String baseName, ZipOutputStream zos) throws IOException {
        File[] files = dir.listFiles();
        byte[] buffer = new byte[4096];

        for (File file : files) {
            if (file.isDirectory()) {
                addDirToZip(file, baseName + file.getName() + "/", zos);
            } else {
                try (FileInputStream fis = new FileInputStream(file)) {
                    zos.putNextEntry(new ZipEntry(baseName + file.getName()));
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, length);
                    }
                    zos.closeEntry();
                }
            }
        }
    }

    private String calculateSha1(File file) throws IOException {
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            try (FileInputStream fis = new FileInputStream(file);
                 DigestInputStream dis = new DigestInputStream(fis, sha1)) {
                while (dis.read() != -1) ;
                byte[] hash = sha1.digest();
                StringBuilder hexString = new StringBuilder();
                for (byte b : hash) {
                    hexString.append(String.format("%02x", b));
                }
                return hexString.toString();
            }
        } catch (Exception e) {
            throw new IOException("Failed to calculate SHA-1 hash.", e);
        }
    }

    private boolean validateJsonFile(File jsonFile) {
        try (BufferedReader reader = new BufferedReader(new FileReader(jsonFile))) {
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line);
            }
            new org.json.JSONObject(jsonContent.toString());
            plugin.getLogger().info("Validated JSON file: " + jsonFile.getName());
            return true;
        } catch (org.json.JSONException e) {
            plugin.getLogger().warning("Invalid JSON syntax in file: " + jsonFile.getName() + ": " + e.getMessage());
            return false;
        } catch (IOException e) {
            plugin.getLogger().warning("Error reading JSON file: " + jsonFile.getName() + ": " + e.getMessage());
            return false;
        }
    }
}