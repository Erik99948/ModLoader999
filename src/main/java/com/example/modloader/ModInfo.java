package com.example.modloader;

import com.example.modloader.api.ModInitializer;

import java.io.File;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Map;

public class ModInfo {
    private String id;
    private String name;
    private String version;
    private String author;
    private String description;
    private String mainClass;
    private Map<String, String> dependencies;
    private List<String> softDependencies;
    private Map<String, String> customProperties;
    private File modFile;
    private transient URLClassLoader classLoader;
    private transient ModInitializer initializer;
    private ModState state;
    private String apiVersion;

    public ModInfo(String id, String name, String version, String author, String description, String mainClass, Map<String, String> dependencies, List<String> softDependencies, Map<String, String> customProperties, File modFile, String apiVersion) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.author = author;
        this.description = description;
        this.mainClass = mainClass;
        this.dependencies = dependencies;
        this.softDependencies = softDependencies;
        this.customProperties = customProperties;
        this.modFile = modFile;
        this.apiVersion = apiVersion;
        this.state = ModState.UNLOADED;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMainClass() {
        return mainClass;
    }

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    public Map<String, String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(Map<String, String> dependencies) {
        this.dependencies = dependencies;
    }

    public List<String> getSoftDependencies() {
        return softDependencies;
    }

    public void setSoftDependencies(List<String> softDependencies) {
        this.softDependencies = softDependencies;
    }

    public Map<String, String> getCustomProperties() {
        return customProperties;
    }

    public void setCustomProperties(Map<String, String> customProperties) {
        this.customProperties = customProperties;
    }

    public File getModFile() {
        return modFile;
    }

    public void setModFile(File modFile) {
        this.modFile = modFile;
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

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }
}