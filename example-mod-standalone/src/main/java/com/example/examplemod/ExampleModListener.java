package com.example.examplemod;

import com.example.modloader.api.event.ModPreLoadEvent;
import com.example.modloader.api.event.SubscribeEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class ExampleModListener {

    private final Logger logger;

    public ExampleModListener(JavaPlugin plugin) {
        this.logger = plugin.getLogger();
    }

    @SubscribeEvent
    public void onModPreLoad(ModPreLoadEvent event) {
        logger.info("ExampleModListener: Mod " + event.getModInfo().getName() + " is pre-loading!");
    }
}