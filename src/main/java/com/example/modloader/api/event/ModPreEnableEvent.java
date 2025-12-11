package com.example.modloader.api.event;

import com.example.modloader.ModInfo;

public class ModPreEnableEvent extends ModEvent {
    private final ModInfo modInfo;

    public ModPreEnableEvent(ModInfo modInfo) {
        this.modInfo = modInfo;
    }

    public ModInfo getModInfo() {
        return modInfo;
    }
}

