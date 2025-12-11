package com.example.modloader.api.dependencyinjection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModAPIRegistry {

    private final Map<Class<?>, List<Object>> apiInstances = new HashMap<>();

    public <T> void registerAPI(Class<T> apiType, T instance) {
        apiInstances.computeIfAbsent(apiType, k -> new ArrayList<>()).add(instance);
    }

    public <T> T getAPI(Class<T> apiType) {
        List<Object> instances = apiInstances.get(apiType);
        if (instances != null && !instances.isEmpty()) {
            return (T) instances.get(0);
        }
        return null;
    }

    public <T> List<T> getAPIs(Class<T> apiType) {
        List<Object> instances = apiInstances.get(apiType);
        if (instances != null) {
            return (List<T>) new ArrayList<>(instances);
        }
        return new ArrayList<>();
    }

    public boolean hasAPI(Class<?> apiType) {
        return apiInstances.containsKey(apiType) && !apiInstances.get(apiType).isEmpty();
    }
}

