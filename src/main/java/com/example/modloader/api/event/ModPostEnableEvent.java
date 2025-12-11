package com.example.modloader.api.event;

import com.example.modloader.ModInfo;

public class ModPostEnableEvent extends ModEvent {
    private final ModInfo modInfo;

    public ModPostEnableEvent(ModInfo modInfo) {
        this.modInfo = modInfo;
    }

    public ModInfo getModInfo() {
        return modInfo;
    }
}

