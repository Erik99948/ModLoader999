package com.example.modloader;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class CustomEventListenerRegistry {

    private final Plugin plugin;
    private final Logger logger;
    private final PluginManager pluginManager;
    private final Map<Listener, String> registeredListenerMods = new HashMap<>();
    private final List<Listener> registeredListeners = new ArrayList<>();

    public CustomEventListenerRegistry(Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.pluginManager = plugin.getServer().getPluginManager();
    }

    public void register(Listener listener, String modId) {
        pluginManager.registerEvents(listener, plugin);
        registeredListeners.add(listener);
        registeredListenerMods.put(listener, modId);
        logger.info("Registered custom event listener: " + listener.getClass().getName() + " by mod " + modId);
    }

    public void unregisterAll(String modId) {
        List<Listener> listenersToRemove = new ArrayList<>();
        for (Map.Entry<Listener, String> entry : registeredListenerMods.entrySet()) {
            if (entry.getValue().equals(modId)) {
                listenersToRemove.add(entry.getKey());
            }
        }

        for (Listener listener : listenersToRemove) {
            HandlerList.unregisterAll(listener);
            registeredListeners.remove(listener);
            registeredListenerMods.remove(listener);
            logger.info("Unregistered custom event listener: " + listener.getClass().getName() + " from mod " + modId);
        }
    }

    public void unregisterAll() {
        for (Listener listener : new ArrayList<>(registeredListeners)) {
            HandlerList.unregisterAll(listener);
            logger.info("Unregistered custom event listener: " + listener.getClass().getName());
        }
        registeredListeners.clear();
        registeredListenerMods.clear();
    }
}