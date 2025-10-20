package com.example.modloader.api;

public interface ModInitializer {

    void onLoad(ModAPI api);

    default void onEnable() {
    }

    default void onDisable() {
    }
}
