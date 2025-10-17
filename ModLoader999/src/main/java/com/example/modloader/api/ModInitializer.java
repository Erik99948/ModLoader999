package com.example.modloader.api;

/**
 * The main interface for a custom mod.
 * Classes implementing this interface will be discovered and loaded by the ModLoader.
 */
public interface ModInitializer {

    /**
     * Called when the ModLoader is initializing the mod.
     * Use this method to register all your custom content using the provided ModAPI.
     *
     * @param api The ModAPI instance provided by the ModLoader.
     */
    void onLoad(ModAPI api);

    /**
     * Called when the ModLoader is enabling the mod.
     * This is typically where you would register event listeners or start tasks.
     */
    default void onEnable() {
        // Default empty implementation
    }

    /**
     * Called when the ModLoader is disabling the mod.
     * Use this method to clean up any resources or unregister listeners.
     */
    default void onDisable() {
        // Default empty implementation
    }
}
