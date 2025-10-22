package com.example.modloader;

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
