package com.example.modloader.api.event;

import com.example.modloader.ModInfo;

public class ModPreLoadEvent extends ModEvent {
    private final ModInfo modInfo;

    public ModPreLoadEvent(ModInfo modInfo) {
        this.modInfo = modInfo;
    }

    public ModInfo getModInfo() {
        return modInfo;
    }
}
