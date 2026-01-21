package com.example.modloader.api;

/**
 * Handler for mod messages.
 */
@FunctionalInterface
public interface ModMessageHandler {
    void handleMessage(String senderModId, String messageType, String payload);
}
