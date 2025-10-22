package com.example.modloader.api;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ModMessageAPIImpl implements ModMessageAPI {

    private final JavaPlugin plugin;
    private final String senderModId;

    private final Map<String, List<HandlerEntry>> messageHandlers = new ConcurrentHashMap<>();
    private final Map<String, Queue<Message>> messageQueues = new ConcurrentHashMap<>();

    public ModMessageAPIImpl(JavaPlugin plugin, String senderModId) {
        this.plugin = plugin;
        this.senderModId = senderModId;
    }

    @Override
    public void sendMessage(String recipientModId, String messageType, String payload) {
        Message message = new Message(senderModId, messageType, payload);
        messageQueues.computeIfAbsent(recipientModId, k -> new ConcurrentLinkedQueue<>()).offer(message);
        plugin.getLogger().info("Mod " + senderModId + " sent message of type " + messageType + " to " + recipientModId);
    }

    @Override
    public void broadcastMessage(String messageType, String payload) {
        Message message = new Message(senderModId, messageType, payload);
        Set<String> recipientModIds = messageHandlers.getOrDefault(messageType, new ArrayList<>()).stream()
                .map(entry -> entry.modId)
                .collect(Collectors.toSet());

        if (recipientModIds.isEmpty()) {
            plugin.getLogger().info("Mod " + senderModId + " broadcasted message of type " + messageType + ", but no mods registered handlers.");
            return;
        }

        for (String recipientModId : recipientModIds) {
            if (!recipientModId.equals(senderModId)) {
                messageQueues.computeIfAbsent(recipientModId, k -> new ConcurrentLinkedQueue<>()).offer(message);
            }
        }
        plugin.getLogger().info("Mod " + senderModId + " broadcasted message of type " + messageType + " to " + recipientModIds.size() + " mods.");
    }

    @Override
    public void registerMessageHandler(String messageType, ModMessageHandler handler) {
        messageHandlers.computeIfAbsent(messageType, k -> new ArrayList<>()).add(new HandlerEntry(handler, senderModId));
        plugin.getLogger().info("Mod " + senderModId + " registered handler for message type: " + messageType);
    }

    @Override
    public void unregisterMessageHandler(String messageType, ModMessageHandler handler) {
        List<HandlerEntry> handlers = messageHandlers.get(messageType);
        if (handlers != null) {
            handlers.removeIf(entry -> entry.handler.equals(handler) && entry.modId.equals(senderModId));
            if (handlers.isEmpty()) {
                messageHandlers.remove(messageType);
            }
            plugin.getLogger().info("Mod " + senderModId + " unregistered handler for message type: " + messageType);
        }
    }

    public void dispatchMessages(String recipientModId) {
        Queue<Message> queue = messageQueues.get(recipientModId);
        if (queue != null) {
            while (!queue.isEmpty()) {
                Message message = queue.poll();
                if (message != null) {
                    List<HandlerEntry> handlers = messageHandlers.get(message.messageType);
                    if (handlers != null) {
                        for (HandlerEntry entry : new ArrayList<>(handlers)) {
                            if (entry.modId.equals(recipientModId)) {
                                try {
                                    entry.handler.handleMessage(message.senderModId, message.messageType, message.payload);
                                } catch (Exception e) {
                                    plugin.getLogger().log(Level.SEVERE, "Error handling message for mod " + recipientModId + " from " + message.senderModId + " type " + message.messageType, e);
                                }
                            }
                        }
                    } else {
                        plugin.getLogger().warning("Mod " + recipientModId + " received message of type " + message.messageType + " from " + message.senderModId + " but no handler registered.");
                    }
                }
            }
        }
    }

    public void unregisterAllHandlersForMod(String modId) {
        messageHandlers.forEach((messageType, handlers) -> {
            handlers.removeIf(entry -> entry.modId.equals(modId));
            if (handlers.isEmpty()) {
                messageHandlers.remove(messageType);
            }
        });
        messageQueues.remove(modId);
        plugin.getLogger().info("Unregistered all message handlers and cleared message queue for mod: " + modId);
    }

    private static class Message {
        final String senderModId;
        final String messageType;
        final String payload;

        Message(String senderModId, String messageType, String payload) {
            this.senderModId = senderModId;
            this.messageType = messageType;
            this.payload = payload;
        }
    }

    private static class HandlerEntry {
        final ModMessageHandler handler;
        final String modId;

        HandlerEntry(ModMessageHandler handler, String modId) {
            this.handler = handler;
            this.modId = modId;
        }
    }
}