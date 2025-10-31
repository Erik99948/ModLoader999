package com.example.modloader.api.dependencyinjection;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Binder {

    private final Map<BindingKey, Object> bindings = new HashMap<>();
    private final Map<BindingKey, ProviderBinding> providerBindings = new HashMap<>();

    public <T> void bind(Class<T> type, T instance) {
        bind(type, instance, null);
    }

    public <T> void bind(Class<T> type, T instance, String name) {
        bindings.put(new BindingKey(type, name), instance);
    }

    public <T> T getInstance(Class<T> type) {
        return getInstance(type, null);
    }

    public <T> T getInstance(Class<T> type, String name) {
        return (T) bindings.get(new BindingKey(type, name));
    }

    public boolean hasBinding(Class<?> type) {
        return hasBinding(type, null);
    }

    public boolean hasBinding(Class<?> type, String name) {
        return bindings.containsKey(new BindingKey(type, name));
    }

    public void registerProviders(Object providerContainer) {
        for (Method method : providerContainer.getClass().getMethods()) {
            if (method.isAnnotationPresent(Provides.class)) {
                Class<?> returnType = method.getReturnType();
                String name = null;
                if (method.isAnnotationPresent(Named.class)) {
                    name = method.getAnnotation(Named.class).value();
                }
                providerBindings.put(new BindingKey(returnType, name), new ProviderBinding(method, providerContainer));
            }
        }
    }

    public ProviderBinding getProvider(Class<?> type, String name) {
        return providerBindings.get(new BindingKey(type, name));
    }

    public boolean hasProvider(Class<?> type, String name) {
        return providerBindings.containsKey(new BindingKey(type, name));
    }

    public static class ProviderBinding {
        private final Method method;
        private final Object instance;

        public ProviderBinding(Method method, Object instance) {
            this.method = method;
            this.instance = instance;
        }

        public Method getMethod() {
            return method;
        }

        public Object getInstance() {
            return instance;
        }
    }

    private static class BindingKey {
        private final Class<?> type;
        private final String name;

        public BindingKey(Class<?> type, String name) {
            this.type = type;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BindingKey that = (BindingKey) o;
            return type.equals(that.type) && Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, name);
        }
    }
}
