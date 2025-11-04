package com.example.modloader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Marketplace {

    private final ModRepository modRepository;
    private final File modsDir;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final ObjectMapper objectMapper = new ObjectMapper();


    public Marketplace(ModRepository modRepository, File modsDir) {
        this.modRepository = modRepository;
        this.modsDir = modsDir;
    }

    public void handleGetMods(HttpExchange httpExchange) throws IOException {
        List<ModInfo> repoMods = modRepository.getAllMods();
        String jsonResponse = gson.toJson(repoMods);
        httpExchange.getResponseHeaders().set("Content-Type", "application/json");
        httpExchange.sendResponseHeaders(200, jsonResponse.length());
        try (OutputStream os = httpExchange.getResponseBody()) {
            os.write(jsonResponse.getBytes());
        }
    }

    public void handleDownloadMod(HttpExchange httpExchange) throws IOException {
        String path = httpExchange.getRequestURI().getPath();
        String[] pathParts = path.split("/");
        if (pathParts.length < 4) {
            httpExchange.sendResponseHeaders(400, -1); // Bad Request
            return;
        }
        String modId = pathParts[3];
        Optional<File> modFile = findModFile(modId);

        if (modFile.isPresent()) {
            File file = modFile.get();
            httpExchange.getResponseHeaders().set("Content-Type", "application/java-archive");
            httpExchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
            httpExchange.sendResponseHeaders(200, file.length());
            try (OutputStream os = httpExchange.getResponseBody()) {
                Files.copy(file.toPath(), os);
            }
        } else {
            httpExchange.sendResponseHeaders(404, -1); // Not Found
        }
    }

    private Optional<File> findModFile(String modId) {
        File[] files = modsDir.listFiles((dir, name) -> name.endsWith(".modloader999"));
        if (files == null) {
            return Optional.empty();
        }
        for (File file : files) {
            try (JarFile jarFile = new JarFile(file)) {
                JarEntry modInfoEntry = jarFile.getJarEntry("modinfo.json");
                if (modInfoEntry != null) {
                    try (InputStream is = jarFile.getInputStream(modInfoEntry)) {
                        JsonNode modInfoJson = objectMapper.readTree(is);
                        if (modInfoJson.has("id") && modId.equals(modInfoJson.get("id").asText())) {
                            return Optional.of(file);
                        }
                    }
                }
            } catch (IOException e) {

                e.printStackTrace();
            }
        }
        return Optional.empty();
    }
}