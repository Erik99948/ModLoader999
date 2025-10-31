package com.example.modloader.api.event;

import com.example.modloader.ModInfo;

public class ModPostDisableEvent extends ModEvent {
    private final ModInfo modInfo;

    public ModPostDisableEvent(ModInfo modInfo) {
        this.modInfo = modInfo;
    }

    public ModInfo getModInfo() {
        return modInfo;
    }
}
