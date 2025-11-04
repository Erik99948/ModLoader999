package com.example.modloader.api;

import com.example.modloader.api.network.MessagePacket;
import com.example.modloader.api.network.Networking;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ModMessageAPIImpl implements ModMessageAPI {

    private final JavaPlugin plugin;
    private final String senderModId;
    private final Networking networking;
    private final String messageChannel = "modloader:messages";

    private final Map<String, List<HandlerEntry>> messageHandlers = new ConcurrentHashMap<>();

    public ModMessageAPIImpl(JavaPlugin plugin, String senderModId, Networking networking) {
        this.plugin = plugin;
        this.senderModId = senderModId;
        this.networking = networking;
        this.networking.registerChannel(messageChannel);
        this.networking.registerListener(messageChannel, MessagePacket.class, this::handleNetworkPacket);
    }

    @Override
    public void sendMessage(String recipientModId, String messageType, String payload) {
        MessagePacket packet = new MessagePacket(senderModId, recipientModId, messageType, payload);


        for (Player player : plugin.getServer().getOnlinePlayers()) {
            networking.sendPacket(player, messageChannel, packet);
        }
        plugin.getLogger().info("Mod " + senderModId + " sent message of type " + messageType + " to " + recipientModId + " via network.");
    }

    @Override
    public void broadcastMessage(String messageType, String payload) {
        MessagePacket packet = new MessagePacket(senderModId, null, messageType, payload);
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            networking.sendPacket(player, messageChannel, packet);
        }
        plugin.getLogger().info("Mod " + senderModId + " broadcasted message of type " + messageType + " via network.");
    }

    @Override
    public void sendInterServerMessage(String targetServer, String recipientModId, String messageType, String payload) {
        Player player = getAnyOnlinePlayer();
        if (player == null) {
            plugin.getLogger().warning("Cannot send inter-server message: No players online to forward the message.");
            return;
        }
        MessagePacket packet = new MessagePacket(senderModId, recipientModId, messageType, payload);
        networking.sendBungeeCordPacket(player, "Forward", targetServer, packet);
        plugin.getLogger().info("Mod " + senderModId + " sent inter-server message of type " + messageType + " to " + recipientModId + " on server " + targetServer);
    }

    @Override
    public void broadcastInterServerMessage(String messageType, String payload) {
        Player player = getAnyOnlinePlayer();
        if (player == null) {
            plugin.getLogger().warning("Cannot broadcast inter-server message: No players online to forward the message.");
            return;
        }
        MessagePacket packet = new MessagePacket(senderModId, null, messageType, payload);
        networking.sendBungeeCordPacket(player, "Forward", "ALL", packet);
        plugin.getLogger().info("Mod " + senderModId + " broadcasted inter-server message of type " + messageType + " to ALL servers.");
    }

    private Player getAnyOnlinePlayer() {
        if (plugin.getServer().getOnlinePlayers().isEmpty()) {
            return null;
        }
        return plugin.getServer().getOnlinePlayers().iterator().next();
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

    private void handleNetworkPacket(MessagePacket packet) {
        if (packet.getRecipientModId() == null || packet.getRecipientModId().equals(senderModId)) {
            List<HandlerEntry> handlers = messageHandlers.get(packet.getMessageType());
            if (handlers != null) {


                List<HandlerEntry> relevantHandlers = handlers.stream()
                        .filter(entry -> packet.getRecipientModId() == null || entry.modId.equals(packet.getRecipientModId()))
                        .collect(Collectors.toList());

                for (HandlerEntry entry : relevantHandlers) {
                    try {
                        entry.handler.handleMessage(packet.getSenderModId(), packet.getMessageType(), packet.getPayload());
                    } catch (Exception e) {
                        plugin.getLogger().log(Level.SEVERE, "Error handling network message for mod " + entry.modId + " from " + packet.getSenderModId() + " type " + packet.getMessageType(), e);
                    }
                }
            } else {
                plugin.getLogger().warning("Received network message of type " + packet.getMessageType() + " from " + packet.getSenderModId() + " but no handler registered.");
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
        plugin.getLogger().info("Unregistered all message handlers for mod: " + modId);
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
