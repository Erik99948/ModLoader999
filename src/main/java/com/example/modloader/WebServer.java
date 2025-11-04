package com.example.modloader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.logging.Level;

public class WebServer {

    private final JavaPlugin plugin;
    private final ModLoaderService modLoaderService;
    private final File resourcePackFile;
    private final int port;
    private HttpServer server;
    private String resourcePackUrl;
    private String dashboardUrl;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Marketplace marketplace;

    public WebServer(JavaPlugin plugin, ModLoaderService modLoaderService, File resourcePackFile, int port, File modsDir) {
        this.plugin = plugin;
        this.modLoaderService = modLoaderService;
        this.resourcePackFile = resourcePackFile;
        this.port = port;
        this.marketplace = new Marketplace(modLoaderService.getModRepository(), modsDir);
    }

    public void start() {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.setExecutor(Executors.newFixedThreadPool(4));


            server.createContext("/", this::handleStaticFileRequest);


            server.createContext("/api/mods", httpExchange -> {
                if ("GET".equals(httpExchange.getRequestMethod())) {
                    List<ModInfo> mods = modLoaderService.getLoadedModsInfo();
                    sendJsonResponse(httpExchange, 200, gson.toJson(mods));
                } else {
                    sendErrorResponse(httpExchange, 405, "Method Not Allowed");
                }
            });


            server.createContext("/api/mod/", httpExchange -> {
                String path = httpExchange.getRequestURI().getPath();
                String[] pathParts = path.split("/");
                if (pathParts.length >= 4 && "mod".equals(pathParts[2])) {
                    String modId = pathParts[3];
                    ModInfo modInfo = modLoaderService.getModInfo(modId);
                    if (modInfo != null) {
                        sendJsonResponse(httpExchange, 200, gson.toJson(modInfo));
                    } else {
                        sendErrorResponse(httpExchange, 404, "Mod Not Found");
                    }
                } else {
                    sendErrorResponse(httpExchange, 400, "Bad Request");
                }
            });


            server.createContext("/api/mod/enable/", httpExchange -> {
                if ("POST".equals(httpExchange.getRequestMethod())) {
                    String path = httpExchange.getRequestURI().getPath();
                    String[] pathParts = path.split("/");
                    if (pathParts.length >= 5 && "enable".equals(pathParts[3])) {
                        String modId = pathParts[4];
                        try {
                            modLoaderService.enableMod(modId);
                            sendJsonResponse(httpExchange, 200, "{\"status\":\"success\", \"message\":\"Mod " + modId + " enabled.\"}");
                        } catch (Exception e) {
                            plugin.getLogger().log(Level.SEVERE, "Failed to enable mod " + modId, e);
                            sendErrorResponse(httpExchange, 500, "Failed to enable mod: " + e.getMessage());
                        }
                    } else {
                        sendErrorResponse(httpExchange, 400, "Bad Request");
                    }
                } else {
                    sendErrorResponse(httpExchange, 405, "Method Not Allowed");
                }
            });


            server.createContext("/api/mod/disable/", httpExchange -> {
                if ("POST".equals(httpExchange.getRequestMethod())) {
                    String path = httpExchange.getRequestURI().getPath();
                    String[] pathParts = path.split("/");
                    if (pathParts.length >= 5 && "disable".equals(pathParts[3])) {
                        String modId = pathParts[4];
                        try {
                            modLoaderService.disableMod(modId);
                            sendJsonResponse(httpExchange, 200, "{\"status\":\"success\", \"message\":\"Mod " + modId + " disabled.\"}");
                        } catch (Exception e) {
                            plugin.getLogger().log(Level.SEVERE, "Failed to disable mod " + modId, e);
                            sendErrorResponse(httpExchange, 500, "Failed to disable mod: " + e.getMessage());
                        }
                    } else {
                        sendErrorResponse(httpExchange, 400, "Bad Request");
                    }
                } else {
                    sendErrorResponse(httpExchange, 405, "Method Not Allowed");
                }
            });


            server.createContext("/api/mod/hotreload/", httpExchange -> {
                if ("POST".equals(httpExchange.getRequestMethod())) {
                    String path = httpExchange.getRequestURI().getPath();
                    String[] pathParts = path.split("/");
                    if (pathParts.length >= 5 && "hotreload".equals(pathParts[3])) {
                        String modId = pathParts[4];
                        try {
                            modLoaderService.hotReloadMod(modId);
                            sendJsonResponse(httpExchange, 200, "{\"status\":\"success\", \"message\":\"Mod " + modId + " hot-reloaded.\"}");
                        } catch (Exception e) {
                            plugin.getLogger().log(Level.SEVERE, "Failed to hot-reload mod " + modId, e);
                            sendErrorResponse(httpExchange, 500, "Failed to hot-reload mod: " + e.getMessage());
                        }
                    } else {
                        sendErrorResponse(httpExchange, 400, "Bad Request");
                    }
                } else {
                    sendErrorResponse(httpExchange, 405, "Method Not Allowed");
                }
            });


            server.createContext("/api/mod/config/", httpExchange -> {
                String path = httpExchange.getRequestURI().getPath();
                String[] pathParts = path.split("/");
                if (pathParts.length >= 5 && "config".equals(pathParts[3])) {
                    String modId = pathParts[4];
                    ModInfo modInfo = modLoaderService.getModInfo(modId);
                    if (modInfo == null) {
                        sendErrorResponse(httpExchange, 404, "Mod Not Found");
                        return;
                    }

                    if ("GET".equals(httpExchange.getRequestMethod())) {
 String configContent = modLoaderService.getModConfigManager().getModConfigYamlContent(modId);
                        if (configContent != null) {
                            httpExchange.getResponseHeaders().set("Content-Type", "text/plain");
                            sendResponse(httpExchange, 200, configContent);
                        } else {
                            sendErrorResponse(httpExchange, 404, "Config Not Found");
                        }
                    } else if ("POST".equals(httpExchange.getRequestMethod())) {
                        try (InputStream is = httpExchange.getRequestBody()) {
                            String requestBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                            modLoaderService.getModConfigManager().setModConfigYamlContent(modId, requestBody);
                            sendJsonResponse(httpExchange, 200, "{\"status\":\"success\", \"message\":\"Mod " + modId + " config updated.\"}");
                        } catch (Exception e) {
                            plugin.getLogger().log(Level.SEVERE, "Failed to update config for mod " + modId, e);
                            sendErrorResponse(httpExchange, 500, "Failed to update config: " + e.getMessage());
                        }
                    } else {
                        sendErrorResponse(httpExchange, 405, "Method Not Allowed");
                    }
                } else {
                    sendErrorResponse(httpExchange, 400, "Bad Request");
                }
            });




            server.createContext("/pack.zip", httpExchange -> {
                if (!resourcePackFile.exists()) {
                    sendErrorResponse(httpExchange, 404, "Resource Pack Not Found");
                    return;
                }

                httpExchange.sendResponseHeaders(200, resourcePackFile.length());
                try (OutputStream os = httpExchange.getResponseBody()) {
                    Files.copy(resourcePackFile.toPath(), os);
                }
            });

            server.start();

            String address = "modloadermarketplace.com";
            this.resourcePackUrl = "http://" + address + ":" + port + "/pack.zip";
            this.dashboardUrl = "http://" + address + ":" + port + "/";

        } catch (IOException e) {
            plugin.getLogger().severe("Failed to start web server on port " + port);
            e.printStackTrace();
        }
    }

    private void handleStaticFileRequest(HttpExchange httpExchange) throws IOException {
        String path = httpExchange.getRequestURI().getPath();
        if (path.equals("/")) {
            path = "/index.html";
        }

        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        String resourcePath = "dashboard/" + path;

        try (InputStream in = plugin.getResource(resourcePath)) {
            if (in == null) {
                sendErrorResponse(httpExchange, 404, "Not Found");
                return;
            }

            String contentType = Files.probeContentType(java.nio.file.Paths.get(path));
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            httpExchange.getResponseHeaders().set("Content-Type", contentType);
            httpExchange.sendResponseHeaders(200, in.available());
            try (OutputStream os = httpExchange.getResponseBody()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }
        }
    }

    private void sendResponse(HttpExchange httpExchange, int statusCode, String response) throws IOException {
        httpExchange.sendResponseHeaders(statusCode, response.length());
        try (OutputStream os = httpExchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }

    public String getResourcePackUrl() {
        return resourcePackUrl;
    }

    public String getDashboardUrl() {

        if (dashboardUrl == null || dashboardUrl.contains("127.0.0.1")) {
            String address = "modloadermarketplace.com";
            return "http://" + address + ":" + port + "/";
        }
        return dashboardUrl;
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            plugin.getLogger().info("Web server stopped.");
        }
    }

    private void sendJsonResponse(HttpExchange httpExchange, int statusCode, String jsonResponse) throws IOException {
        httpExchange.getResponseHeaders().set("Content-Type", "application/json");
        sendResponse(httpExchange, statusCode, jsonResponse);
    }

    private void sendErrorResponse(HttpExchange httpExchange, int statusCode, String message) throws IOException {
        JsonObject errorResponse = new JsonObject();
        errorResponse.addProperty("status", "error");
        errorResponse.addProperty("message", message);
        sendJsonResponse(httpExchange, statusCode, gson.toJson(errorResponse));
    }
}