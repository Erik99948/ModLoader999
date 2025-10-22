package com.example.modloader.api.dependencyinjection;

import java.util.HashMap;
import java.util.Map;

public class ModAPIRegistry {

    private final Map<Class<?>, Object> apiInstances = new HashMap<>();

    public <T> void registerAPI(Class<T> apiType, T instance) {
        apiInstances.put(apiType, instance);
    }

    public <T> T getAPI(Class<T> apiType) {
        return (T) apiInstances.get(apiType);
    }

    public boolean hasAPI(Class<?> apiType) {
        return apiInstances.containsKey(apiType);
    }
}
