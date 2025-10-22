package com.example.modloader.api.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class EventBus {

    private final Map<Class<? extends ModEvent>, List<Consumer<? extends ModEvent>>> listeners = new HashMap<>();

    public <T extends ModEvent> void register(Class<T> eventType, Consumer<T> listener) {
        listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(listener);
    }

    public <T extends ModEvent> void post(T event) {
        List<Consumer<? extends ModEvent>> eventListeners = listeners.get(event.getClass());
        if (eventListeners != null) {
            for (Consumer listener : eventListeners) {
                listener.accept(event);
            }
        }
    }
}
