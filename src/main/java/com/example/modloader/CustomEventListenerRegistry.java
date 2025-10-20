package com.example.modloader;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class CustomEventListenerRegistry {

    private final Plugin plugin;
    private final Logger logger;
    private final PluginManager pluginManager;
    private final List<Listener> registeredListeners = new ArrayList<>();

    public CustomEventListenerRegistry(Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.pluginManager = plugin.getServer().getPluginManager();
    }

    public void register(Listener listener) {
        pluginManager.registerEvents(listener, plugin);
        registeredListeners.add(listener);
        logger.info("Registered custom event listener: " + listener.getClass().getName());
    }

    public void unregisterAll() {
        for (Listener listener : registeredListeners) {
            HandlerList.unregisterAll(listener);
            logger.info("Unregistered custom event listener: " + listener.getClass().getName());
        }
        registeredListeners.clear();
    }
}
