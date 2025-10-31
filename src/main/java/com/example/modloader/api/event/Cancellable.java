package com.example.modloader.api.event;

public interface Cancellable {
    boolean isCancelled();
    void setCancelled(boolean cancel);
}
