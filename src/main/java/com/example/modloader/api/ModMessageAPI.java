package com.example.modloader.api;

public interface ModMessageAPI {

    void sendMessage(String recipientModId, String messageType, String payload);

    void broadcastMessage(String messageType, String payload);

    void registerMessageHandler(String messageType, ModMessageHandler handler);

    void unregisterMessageHandler(String messageType, ModMessageHandler handler);

    void sendInterServerMessage(String targetServer, String recipientModId, String messageType, String payload);

    void broadcastInterServerMessage(String messageType, String payload);
}
