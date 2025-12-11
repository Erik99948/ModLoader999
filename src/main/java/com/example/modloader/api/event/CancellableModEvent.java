package com.example.modloader.api.event;

public abstract class CancellableModEvent extends ModEvent implements Cancellable {
    private boolean cancelled = false;

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}

