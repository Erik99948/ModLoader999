package com.example.modloader.api.event;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple event bus for dispatching events to registered listeners.
 */
public class EventBus {
    private static final Logger LOGGER = Logger.getLogger(EventBus.class.getName());
    private final Map<Class<?>, List<ListenerMethod>> listeners = new ConcurrentHashMap<>();

    /**
     * Register all event handler methods from the listener object.
     */
    public void register(Object listenerObject) {
        for (Method method : listenerObject.getClass().getMethods()) {
            if (method.getParameterCount() == 1) {
                Class<?> eventType = method.getParameterTypes()[0];
                listeners.computeIfAbsent(eventType, k -> Collections.synchronizedList(new ArrayList<>()))
                    .add(new ListenerMethod(listenerObject, method));
            }
        }
    }

    /**
     * Unregister all event handlers from the listener object.
     */
    public void unregister(Object listenerObject) {
        listeners.values().forEach(list -> 
            list.removeIf(lm -> lm.instance.equals(listenerObject))
        );
    }

    /**
     * Post an event to all registered listeners.
     */
    public void post(Object event) {
        List<ListenerMethod> eventListeners = listeners.get(event.getClass());
        if (eventListeners != null) {
            for (ListenerMethod lm : new ArrayList<>(eventListeners)) {
                try {
                    lm.method.invoke(lm.instance, event);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error dispatching event " + event.getClass().getName(), e);
                }
            }
        }
    }

    /**
     * Check if an event is cancelled (if it's a CancellableEvent).
     */
    public boolean isCancelled(Object event) {
        if (event instanceof CancellableEvent) {
            return ((CancellableEvent) event).isCancelled();
        }
        return false;
    }

    private static class ListenerMethod {
        final Object instance;
        final Method method;

        ListenerMethod(Object instance, Method method) {
            this.instance = instance;
            this.method = method;
            this.method.setAccessible(true);
        }
    }

    // ==================== Base Event Classes ====================

    /**
     * Base class for all events.
     */
    public static class BaseEvent {
    }

    /**
     * Base class for events that can be cancelled.
     */
    public static class CancellableEvent extends BaseEvent {
        private boolean cancelled = false;

        public boolean isCancelled() {
            return cancelled;
        }

        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }
    }
}
