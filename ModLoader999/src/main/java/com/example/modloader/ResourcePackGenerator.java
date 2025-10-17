package com.example.modloader;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ResourcePackGenerator {

    private final JavaPlugin plugin;
    private final File stagingDir;
    private final File zipFile;
    private String zipFileSha1;

    public ResourcePackGenerator(JavaPlugin plugin) {
        this.plugin = plugin;
        this.stagingDir = new File(plugin.getDataFolder(), "resource-pack-staging");
        this.zipFile = new File(plugin.getDataFolder(), "generated-pack.zip");
    }

    public boolean generate() {
        try {
            // 1. Create pack.mcmeta
            createPackMeta();

            // 2. Zip the directory
            zipDirectory(stagingDir, zipFile);

            // 3. Calculate SHA-1 hash
            this.zipFileSha1 = calculateSha1(zipFile);
            plugin.getLogger().info("Generated resource pack with SHA-1: " + this.zipFileSha1);

            return true;
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to generate resource pack zip.");
            e.printStackTrace();
            return false;
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
            // The pack format for 1.21 is 34
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
                // Read the file to update the digest
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
}
