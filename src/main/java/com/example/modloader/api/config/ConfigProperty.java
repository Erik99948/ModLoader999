package com.example.modloader.api.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigProperty {
    String path() default "";
    String defaultValue() default "";
    String description() default "";
    String[] allowedValues() default {};
    double minValue() default Double.MIN_VALUE;
    double maxValue() default Double.MAX_VALUE;
    String pattern() default "";
    boolean required() default false;
}