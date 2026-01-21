package com.example.modloader.api.config;

import java.lang.annotation.*;

/**
 * Interface for mod configuration classes.
 */
public interface ModConfig {
    default void onLoad() {}
    default void onSave() {}
    default void validate() throws ConfigValidationException {}
}

/**
 * Annotation for config properties.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface ConfigProperty {
    String path() default "";
    String defaultValue() default "";
    String description() default "";
    String[] allowedValues() default {};
    double minValue() default Double.MIN_VALUE;
    double maxValue() default Double.MAX_VALUE;
    String pattern() default "";
    boolean required() default false;
}

/**
 * Annotation for config values.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface ConfigValue {
    String value() default "";
}

/**
 * Annotation for config provider methods.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface ModConfigProvider {
    String value() default "config.yml";
}

/**
 * Exception for config validation errors.
 */
class ConfigValidationException extends Exception {
    public ConfigValidationException(String message) {
        super(message);
    }
    
    public ConfigValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
