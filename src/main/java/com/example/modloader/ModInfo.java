package com.example.modloader;

import com.example.modloader.api.ModInitializer;

import java.io.File;
import java.net.URLClassLoader;
import java.util.Map;

public class ModInfo {
    private final String name;
    private final String version;
    private final String author;
    private final String mainClass;
    private final Map<String, String> dependencies;
    private final File modFile;
    private URLClassLoader classLoader;
    private ModInitializer initializer;
    private ModState state;

    public ModInfo(String name, String version, String author, String mainClass, Map<String, String> dependencies, File modFile) {
        this.state = ModState.UNLOADED;
        this.name = name;
        this.version = version;
        this.author = author;
        this.mainClass = mainClass;
        this.dependencies = dependencies;
        this.modFile = modFile;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getAuthor() {
        return author;
    }

    public String getMainClass() {
        return mainClass;
    }

    public Map<String, String> getDependencies() {
        return dependencies;
    }

    public File getModFile() {
        return modFile;
    }

    public URLClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(URLClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public ModInitializer getInitializer() {
        return initializer;
    }

    public void setInitializer(ModInitializer initializer) {
        this.initializer = initializer;
    }

    public ModState getState() {
        return state;
    }

    public void setState(ModState state) {
        this.state = state;
    }
}
