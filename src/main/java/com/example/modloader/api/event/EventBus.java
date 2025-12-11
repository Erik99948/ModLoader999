package com.example.modloader.api.event;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EventBus {

    private static final Logger LOGGER = Logger.getLogger(EventBus.class.getName());
    private final Map<Class<? extends ModEvent>, List<MethodEventListener>> listeners = new HashMap<>();

    public void register(Object listenerObject) {
        for (Method method : listenerObject.getClass().getMethods()) {
            if (method.isAnnotationPresent(SubscribeEvent.class)) {
                if (method.getParameterCount() != 1) {
                    LOGGER.log(Level.WARNING, "Method " + method.getName() + " in " + listenerObject.getClass().getName() + " is annotated with @SubscribeEvent but does not have exactly one parameter. Skipping.");
                    continue;
                }
                Class<?> parameterType = method.getParameterTypes()[0];
                if (!ModEvent.class.isAssignableFrom(parameterType)) {
                    LOGGER.log(Level.WARNING, "Method " + method.getName() + " in " + listenerObject.getClass().getName() + " is annotated with @SubscribeEvent but its parameter is not a ModEvent. Skipping.");
                    continue;
                }

                SubscribeEvent annotation = method.getAnnotation(SubscribeEvent.class);
                EventPriority priority = annotation.priority();
                boolean ignoreCancelled = annotation.ignoreCancelled();

                Class<? extends ModEvent> eventType = (Class<? extends ModEvent>) parameterType;
                listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(new MethodEventListener(listenerObject, method, priority, ignoreCancelled));
                listeners.get(eventType).sort(Comparator.comparing(MethodEventListener::getPriority).reversed());
            }
        }
    }

    public <T extends ModEvent> void post(T event) {
        List<MethodEventListener> eventListeners = listeners.get(event.getClass());
        if (eventListeners != null) {
            for (MethodEventListener listener : eventListeners) {
                if (event instanceof Cancellable && ((Cancellable) event).isCancelled() && listener.isIgnoreCancelled()) {
                    continue;
                }
                try {
                    listener.getMethod().invoke(listener.getTarget(), event);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error dispatching event " + event.getClass().getName() + " to listener " + listener.getTarget().getClass().getName() + ".", e);
                }
            }
        }
    }

    public void unregister(Object listenerObject) {
        listeners.forEach((eventType, methodEventListeners) ->
                methodEventListeners.removeIf(listener -> listener.getTarget().equals(listenerObject))
        );
    }

    private static class MethodEventListener {
        private final Object target;
        private final Method method;
        private final EventPriority priority;
        private final boolean ignoreCancelled;

        public MethodEventListener(Object target, Method method, EventPriority priority, boolean ignoreCancelled) {
            this.target = target;
            this.method = method;
            this.priority = priority;
            this.ignoreCancelled = ignoreCancelled;
            this.method.setAccessible(true);
        }

        public Object getTarget() {
            return target;
        }

        public Method getMethod() {
            return method;
        }

        public EventPriority getPriority() {
            return priority;
        }

        public boolean isIgnoreCancelled() {
            return ignoreCancelled;
        }
    }
}

