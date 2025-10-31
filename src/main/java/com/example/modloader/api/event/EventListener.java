package com.example.modloader.api.event;

import java.util.function.Consumer;

class EventListener {
    private final Consumer<? extends ModEvent> listener;
    private final EventPriority priority;

    public EventListener(Consumer<? extends ModEvent> listener, EventPriority priority) {
        this.listener = listener;
        this.priority = priority;
    }

    public Consumer<? extends ModEvent> getListener() {
        return listener;
    }

    public EventPriority getPriority() {
        return priority;
    }
}
