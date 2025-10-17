package com.example.modloader;

import com.sun.net.httpserver.HttpServer;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;

public class WebServer {

    private final JavaPlugin plugin;
    private final File resourcePackFile;
    private final int port;
    private HttpServer server;
    private String resourcePackUrl;

    public WebServer(JavaPlugin plugin, File resourcePackFile, int port) {
        this.plugin = plugin;
        this.resourcePackFile = resourcePackFile;
        this.port = port;
    }

    public void start() {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/pack.zip", httpExchange -> {
                if (!resourcePackFile.exists()) {
                    String response = "404 (Not Found)\n";
                    httpExchange.sendResponseHeaders(404, response.length());
                    try (OutputStream os = httpExchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                    return;
                }

                httpExchange.sendResponseHeaders(200, resourcePackFile.length());
                try (OutputStream os = httpExchange.getResponseBody()) {
                    Files.copy(resourcePackFile.toPath(), os);
                }
            });

            server.setExecutor(null); // Use the default executor
            server.start();

            // Determine the URL
            String address = plugin.getServer().getIp();
            if (address == null || address.isEmpty() || address.equals("0.0.0.0")) {
                address = "127.0.0.1"; // Fallback for local hosting
            }
            this.resourcePackUrl = "http://" + address + ":" + port + "/pack.zip";

            plugin.getLogger().info("Web server started on port " + port);
            plugin.getLogger().info("Resource pack is available at: " + this.resourcePackUrl);

        } catch (IOException e) {
            plugin.getLogger().severe("Failed to start web server on port " + port);
            e.printStackTrace();
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            plugin.getLogger().info("Web server stopped.");
        }
    }

    public String getResourcePackUrl() {
        return resourcePackUrl;
    }
}
