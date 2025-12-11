package com.example.modloader.api;

@FunctionalInterface
public interface ModMessageHandler {
    void handleMessage(String senderModId, String messageType, String payload);
}
