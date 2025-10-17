package com.example.modloader;

import org.bukkit.entity.EntityType;
import org.bukkit.plugin.Plugin;

import java.util.logging.Logger;

/**
 * A service for registering custom mobs.
 * An instance of this class is provided by the ModLoader engine.
 */
public class CustomMobRegistry {

    private final Logger logger;

    public CustomMobRegistry(Plugin plugin) {
        this.logger = plugin.getLogger();
    }

    /**
     * Registers a custom mob with the engine.
     * (Currently, this just logs the registration).
     *
     * @param mobId A unique identifier for your mob.
     * @param baseType The vanilla mob type to base this custom mob on.
     */
    public void register(String mobId, EntityType baseType) {
        // In the future, this will add the mob to a proper registry.
        logger.info("Registering custom mob: " + mobId + " (based on " + baseType.name() + ")");
    }
}
