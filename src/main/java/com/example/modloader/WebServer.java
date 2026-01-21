package com.example.modloader;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.concurrent.Executors;
import java.util.logging.Level;

public class WebServer {
    private final JavaPlugin plugin;
    private final ModLoaderService modLoaderService;
    private final File resourcePackFile;
    private final int port;
    private final File modsFolder;
    private HttpServer server;
    private String resourcePackUrl;
    private byte[] resourcePackHash;

    public WebServer(JavaPlugin plugin, ModLoaderService modLoaderService, File resourcePackFile, int port, File modsFolder) {
        this.plugin = plugin;
        this.modLoaderService = modLoaderService;
        this.resourcePackFile = resourcePackFile;
        this.port = port;
        this.modsFolder = modsFolder;
    }

    public void start() {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.setExecutor(Executors.newFixedThreadPool(4));

            server.createContext("/resourcepack", this::handleResourcePack);
            server.createContext("/api/mods", this::handleModsList);
            server.createContext("/api/mod/", this::handleModDownload);
            server.createContext("/health", this::handleHealth);

            server.start();
            
            String serverIp = plugin.getServer().getIp();
            if (serverIp == null || serverIp.isEmpty()) {
                serverIp = "localhost";
            }
            resourcePackUrl = "http://" + serverIp + ":" + port + "/resourcepack";
            
            if (resourcePackFile != null && resourcePackFile.exists()) {
                resourcePackHash = calculateHash(resourcePackFile);
            }
            
            plugin.getLogger().info("Web server started on port " + port);
            plugin.getLogger().info("Resource pack URL: " + resourcePackUrl);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to start web server", e);
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            plugin.getLogger().info("Web server stopped.");
        }
    }

    private void handleResourcePack(HttpExchange exchange) throws IOException {
        if (resourcePackFile == null || !resourcePackFile.exists()) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }

        exchange.getResponseHeaders().set("Content-Type", "application/zip");
        exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"resources.zip\"");
        exchange.sendResponseHeaders(200, resourcePackFile.length());

        try (OutputStream os = exchange.getResponseBody()) {
            Files.copy(resourcePackFile.toPath(), os);
        }
    }

    private void handleModsList(HttpExchange exchange) throws IOException {
        StringBuilder json = new StringBuilder("[");
        boolean first = true;
        
        for (ModInfo mod : modLoaderService.getLoadedMods().values()) {
            if (!first) json.append(",");
            first = false;
            json.append("{")
                .append("\"id\":\"").append(escapeJson(mod.getId())).append("\",")
                .append("\"name\":\"").append(escapeJson(mod.getName())).append("\",")
                .append("\"version\":\"").append(escapeJson(mod.getVersion())).append("\",")
                .append("\"author\":\"").append(escapeJson(mod.getAuthor())).append("\",")
                .append("\"description\":\"").append(escapeJson(mod.getDescription())).append("\"")
                .append("}");
        }
        json.append("]");

        byte[] response = json.toString().getBytes();
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }

    private void handleModDownload(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String modId = path.substring("/api/mod/".length());
        
        ModInfo mod = modLoaderService.getMod(modId);
        if (mod == null || mod.getModFile() == null || !mod.getModFile().exists()) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }

        File modFile = mod.getModFile();
        exchange.getResponseHeaders().set("Content-Type", "application/java-archive");
        exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + modFile.getName() + "\"");
        exchange.sendResponseHeaders(200, modFile.length());

        try (OutputStream os = exchange.getResponseBody()) {
            Files.copy(modFile.toPath(), os);
        }
    }

    private void handleHealth(HttpExchange exchange) throws IOException {
        String response = "{\"status\":\"ok\",\"mods\":" + modLoaderService.getLoadedMods().size() + "}";
        byte[] bytes = response.getBytes();
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, bytes.length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private byte[] calculateHash(File file) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-1");
            try (InputStream is = new FileInputStream(file)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    digest.update(buffer, 0, read);
                }
            }
            return digest.digest();
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to calculate resource pack hash", e);
            return new byte[0];
        }
    }

    public String getResourcePackUrl() {
        return resourcePackUrl;
    }

    public byte[] getResourcePackHash() {
        return resourcePackHash;
    }
}
