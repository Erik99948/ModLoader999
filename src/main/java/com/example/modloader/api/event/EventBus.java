package com.example.modloader.api.event;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class EventBus {

    private final Map<Class<? extends ModEvent>, List<EventListener>> listeners = new HashMap<>();

    public <T extends ModEvent> void register(Class<T> eventType, Consumer<T> listener) {
        register(eventType, listener, EventPriority.NORMAL);
    }

    public <T extends ModEvent> void register(Class<T> eventType, Consumer<T> listener, EventPriority priority) {
        listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(new EventListener((Consumer<? extends ModEvent>) listener, priority));
        listeners.get(eventType).sort(Comparator.comparing(EventListener::getPriority).reversed());
    }

    public <T extends ModEvent> void post(T event) {
        List<EventListener> eventListeners = listeners.get(event.getClass());
        if (eventListeners != null) {
            for (EventListener listener : eventListeners) {
                if (event instanceof Cancellable && ((Cancellable) event).isCancelled() && listener.getPriority() != EventPriority.MONITOR) {
                    continue;
                }
                ((Consumer<T>) listener.getListener()).accept(event);
            }
        }
    }
}
