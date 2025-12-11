package com.example.modloader.api.event;

import com.example.modloader.ModInfo;

public class ModPostLoadEvent extends ModEvent {
    private final ModInfo modInfo;

    public ModPostLoadEvent(ModInfo modInfo) {
        this.modInfo = modInfo;
    }

    public ModInfo getModInfo() {
        return modInfo;
    }
}

