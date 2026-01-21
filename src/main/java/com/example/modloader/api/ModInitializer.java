package com.example.modloader.api;

/**
 * Interface that all mod main classes must implement.
 * Provides lifecycle hooks for mod initialization and cleanup.
 */
public interface ModInitializer {
    
    /**
     * Called before the mod is loaded. Use for early setup.
     */
    default void onPreLoad(ModAPI api) {}
    
    /**
     * Called when the mod is being loaded. Register items, blocks, etc here.
     */
    default void onLoad(ModAPI api) {}
    
    /**
     * Called after the mod is loaded. Use for post-initialization.
     */
    default void onPostLoad(ModAPI api) {}
    
    /**
     * Called when the mod is enabled.
     */
    default void onEnable() {}
    
    /**
     * Called before the mod is disabled.
     */
    default void onPreDisable() {}
    
    /**
     * Called when the mod is disabled.
     */
    default void onDisable() {}
    
    /**
     * Called after the mod is disabled.
     */
    default void onPostDisable() {}
}
