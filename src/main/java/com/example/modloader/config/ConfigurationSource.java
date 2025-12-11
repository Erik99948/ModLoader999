package com.example.modloader.config;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface ConfigurationSource {
    JsonNode load(InputStream inputStream) throws IOException;
    void save(JsonNode jsonNode, OutputStream outputStream) throws IOException;
}
