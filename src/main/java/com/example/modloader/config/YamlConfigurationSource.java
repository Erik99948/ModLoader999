package com.example.modloader.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class YamlConfigurationSource implements ConfigurationSource {

    private final ObjectMapper yamlMapper;

    public YamlConfigurationSource() {
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
    }

    @Override
    public JsonNode load(InputStream inputStream) throws IOException {
        return yamlMapper.readTree(inputStream);
    }

    @Override
    public void save(JsonNode jsonNode, OutputStream outputStream) throws IOException {
        yamlMapper.writeValue(outputStream, jsonNode);
    }
}