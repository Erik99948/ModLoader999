package com.example.modloader.api.event;

import com.example.modloader.CustomBlock;

public class PreRegisterBlockEvent extends CancellableModEvent {
    private final CustomBlock block;

    public PreRegisterBlockEvent(CustomBlock block) {
        this.block = block;
    }

    public CustomBlock getBlock() {
        return block;
    }
}
