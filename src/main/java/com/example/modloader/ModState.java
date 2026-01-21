package com.example.modloader;

/**
 * Represents the lifecycle state of a mod.
 */
public enum ModState {
    UNLOADED,
    LOADING,
    LOADED,
    INITIALIZING,
    ENABLED,
    DISABLING,
    DISABLED,
    ERRORED
}
