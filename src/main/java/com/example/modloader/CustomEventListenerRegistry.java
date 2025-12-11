package com.example.modloader;

import com.example.modloader.api.event.EventBus;
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
    private final EventBus eventBus;
    private final Map<Object, String> registeredListenerMods = new HashMap<>();
    private final List<Object> registeredListeners = new ArrayList<>();

    public CustomEventListenerRegistry(Plugin plugin, EventBus eventBus) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.pluginManager = plugin.getServer().getPluginManager();
        this.eventBus = eventBus;
    }

    public void register(Object listener, String modId) {
        if (listener instanceof Listener) {
            pluginManager.registerEvents((Listener) listener, plugin);
        }
        eventBus.register(listener);

        registeredListeners.add(listener);
        registeredListenerMods.put(listener, modId);
        logger.info("Registered custom event listener: " + listener.getClass().getName() + " by mod " + modId);
    }

    public void unregisterAll(String modId) {
        List<Object> listenersToRemove = new ArrayList<>();
        for (Map.Entry<Object, String> entry : registeredListenerMods.entrySet()) {
            if (entry.getValue().equals(modId)) {
                listenersToRemove.add(entry.getKey());
            }
        }

        for (Object listener : listenersToRemove) {
            if (listener instanceof Listener) {
                HandlerList.unregisterAll((Listener) listener);
            }
            eventBus.unregister(listener);
            registeredListeners.remove(listener);
            registeredListenerMods.remove(listener);
            logger.info("Unregistered custom event listener: " + listener.getClass().getName() + " from mod " + modId);
        }
    }

    public void unregisterAll() {
        for (Object listener : new ArrayList<>(registeredListeners)) {
            if (listener instanceof Listener) {
                HandlerList.unregisterAll((Listener) listener);
            }
            eventBus.unregister(listener);
            logger.info("Unregistered custom event listener: " + listener.getClass().getName());
        }
        registeredListeners.clear();
        registeredListenerMods.clear();
    }
}
