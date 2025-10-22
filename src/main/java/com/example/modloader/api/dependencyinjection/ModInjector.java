package com.example.modloader.api.dependencyinjection;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class ModInjector {

    private final Binder binder;
    private final ModAPIRegistry apiRegistry;

    public ModInjector(Binder binder, ModAPIRegistry apiRegistry) {
        this.binder = binder;
        this.apiRegistry = apiRegistry;
    }

    public <T> T createInstance(Class<T> type) throws Exception {
        Constructor<?>[] constructors = type.getConstructors();
        if (constructors.length == 0) {
            return type.getDeclaredConstructor().newInstance();
        }

        Constructor<?> constructor = constructors[0];
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        List<Object> parameters = new ArrayList<>();
        for (Class<?> parameterType : parameterTypes) {
            if (binder.hasBinding(parameterType)) {
                parameters.add(binder.getInstance(parameterType));
            } else if (apiRegistry.hasAPI(parameterType)) {
                parameters.add(apiRegistry.getAPI(parameterType));
            } else {
                throw new Exception("No binding found for type: " + parameterType.getName());
            }
        }

        return (T) constructor.newInstance(parameters.toArray());
    }
}
