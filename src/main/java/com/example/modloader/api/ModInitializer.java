package com.example.modloader.api;

import com.example.modloader.api.dependencyinjection.Binder;

public interface ModInitializer {

    void configure(Binder binder);

    void onPreLoad(ModAPI api);

    void onLoad(ModAPI api);

    void onPostLoad(ModAPI api);

    void onEnable();

    void onDisable();

    void onPreDisable();

    void onPostDisable();
}

