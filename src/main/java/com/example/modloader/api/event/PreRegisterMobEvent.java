package com.example.modloader.api.event;

import com.example.modloader.CustomMob;

public class PreRegisterMobEvent extends CancellableModEvent {
    private final CustomMob mob;

    public PreRegisterMobEvent(CustomMob mob) {
        this.mob = mob;
    }

    public CustomMob getMob() {
        return mob;
    }
}
