package com.example.modloader.api.config;

public interface ConfigChangeListener<T extends ModConfig> {
    void onConfigChanged(T newConfig);
}

