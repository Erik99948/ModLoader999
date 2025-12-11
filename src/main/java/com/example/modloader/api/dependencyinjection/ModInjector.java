package com.example.modloader.api.dependencyinjection;

import com.example.modloader.api.dependencyinjection.Binder.ProviderBinding;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ModInjector {

    private final Binder binder;
    private final ModAPIRegistry apiRegistry;
    private final Map<Class<?>, Object> singletons = new ConcurrentHashMap<>();

    public ModInjector(Binder binder, ModAPIRegistry apiRegistry) {
        this.binder = binder;
        this.apiRegistry = apiRegistry;
    }

    public <T> T createInstance(Class<T> type) throws Exception {
        return createInstance(type, null);
    }

    public <T> T createInstance(Class<T> type, String name) throws Exception {
        if (type.isAnnotationPresent(Singleton.class)) {
            @SuppressWarnings("unchecked")
            T instance = (T) singletons.get(type);
            if (instance != null) {
                return instance;
            }
        }

        if (binder.hasBinding(type, name)) {
            return binder.getInstance(type, name);
        }

        if (binder.hasProvider(type, name)) {
            ProviderBinding provider = binder.getProvider(type, name);
            Method method = provider.getMethod();
            List<Object> parameters = new ArrayList<>();
            for (Class<?> parameterType : method.getParameterTypes()) {
                parameters.add(createInstance(parameterType));
            }
            @SuppressWarnings("unchecked")
            T instance = (T) method.invoke(provider.getInstance(), parameters.toArray());
            if (type.isAnnotationPresent(Singleton.class)) {
                singletons.put(type, instance);
            }
            return instance;
        }

        if (apiRegistry.hasAPI(type)) {
            return apiRegistry.getAPI(type);
        }

        Constructor<?>[] constructors = type.getConstructors();
        if (constructors.length == 0) {
            T instance = type.getDeclaredConstructor().newInstance();
            if (type.isAnnotationPresent(Singleton.class)) {
                singletons.put(type, instance);
            }
            return instance;
        }

        Constructor<?> constructor = constructors[0];
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        java.lang.annotation.Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();
        List<Object> parameters = new ArrayList<>();
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            String paramName = null;
            for (java.lang.annotation.Annotation annotation : parameterAnnotations[i]) {
                if (annotation instanceof Named) {
                    paramName = ((Named) annotation).value();
                    break;
                }
            }

            
            if (List.class.isAssignableFrom(parameterType)) {
                java.lang.reflect.Type genericParameterType = constructor.getGenericParameterTypes()[i];
                if (genericParameterType instanceof java.lang.reflect.ParameterizedType) {
                    java.lang.reflect.ParameterizedType pType = (java.lang.reflect.ParameterizedType) genericParameterType;
                    java.lang.reflect.Type[] actualTypeArguments = pType.getActualTypeArguments();
                    if (actualTypeArguments.length == 1 && actualTypeArguments[0] instanceof Class) {
                        Class<?> apiListType = (Class<?>) actualTypeArguments[0];
                        if (apiRegistry.hasAPI(apiListType)) {
                            parameters.add(apiRegistry.getAPIs(apiListType));
                            continue;
                        }
                    }
                }
            }

            parameters.add(createInstance(parameterType, paramName));
        }

        T instance = (T) constructor.newInstance(parameters.toArray());
        if (type.isAnnotationPresent(Singleton.class)) {
            singletons.put(type, instance);
        }
        return instance;
    }
}

