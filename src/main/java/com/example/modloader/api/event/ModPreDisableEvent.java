package com.example.modloader.api.event;

import com.example.modloader.ModInfo;

public class ModPreDisableEvent extends ModEvent {
    private final ModInfo modInfo;

    public ModPreDisableEvent(ModInfo modInfo) {
        this.modInfo = modInfo;
    }

    public ModInfo getModInfo() {
        return modInfo;
    }
}

