package com.example.modloader.api.dependencyinjection;

import java.util.HashMap;
import java.util.Map;

public class Binder {

    private final Map<Class<?>, Object> bindings = new HashMap<>();

    public <T> void bind(Class<T> type, T instance) {
        bindings.put(type, instance);
    }

    public <T> T getInstance(Class<T> type) {
        return (T) bindings.get(type);
    }

    public boolean hasBinding(Class<?> type) {
        return bindings.containsKey(type);
    }
}
