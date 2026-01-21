package com.example.modloader.api.network;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class Networking implements PluginMessageListener {
    private final JavaPlugin plugin;
    private final Map<String, Map<Class<?>, List<Consumer<?>>>> channelListeners = new ConcurrentHashMap<>();
    private final Set<String> registeredChannels = ConcurrentHashMap.newKeySet();

    public Networking(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerChannel(String channelName) {
        if (registeredChannels.add(channelName)) {
            plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, channelName, this);
            plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, channelName);
        }
    }

    public void unregisterChannel(String channelName) {
        if (registeredChannels.remove(channelName)) {
            plugin.getServer().getMessenger().unregisterIncomingPluginChannel(plugin, channelName, this);
            plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(plugin, channelName);
            channelListeners.remove(channelName);
        }
    }

    public <T> void registerListener(String channelName, Class<T> packetType, Consumer<T> listener) {
        if (!registeredChannels.contains(channelName)) {
            registerChannel(channelName);
        }
        channelListeners.computeIfAbsent(channelName, k -> new ConcurrentHashMap<>())
            .computeIfAbsent(packetType, k -> new ArrayList<>())
            .add(listener);
    }

    public void sendPacket(Player player, String channelName, Object packet) {
        if (!registeredChannels.contains(channelName)) return;
        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
            objectStream.writeObject(packet);
            objectStream.flush();
            player.sendPluginMessage(plugin, channelName, byteStream.toByteArray());
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to send packet: " + e.getMessage());
        }
    }

    public void sendBungeeCordPacket(Player player, String subChannel, String serverName, Object packet) {
        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(byteStream);
            out.writeUTF(subChannel);
            out.writeUTF(serverName);
            ByteArrayOutputStream packetByteStream = new ByteArrayOutputStream();
            ObjectOutputStream packetObjectStream = new ObjectOutputStream(packetByteStream);
            packetObjectStream.writeObject(packet);
            packetObjectStream.flush();
            out.writeShort(packetByteStream.toByteArray().length);
            out.write(packetByteStream.toByteArray());
            player.sendPluginMessage(plugin, "BungeeCord", byteStream.toByteArray());
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to send BungeeCord packet: " + e.getMessage());
        }
    }

    public MessagePacket createMessagePacket(String senderModId, String recipientModId, String messageType, String payload) {
        return new MessagePacket(senderModId, recipientModId, messageType, payload);
    }

    public VoicePacket createVoicePacket(byte[] data) {
        return new VoicePacket(data);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onPluginMessageReceived(String channelName, Player player, byte[] message) {
        Map<Class<?>, List<Consumer<?>>> listeners = channelListeners.get(channelName);
        if (listeners == null) return;
        try {
            ByteArrayInputStream byteStream = new ByteArrayInputStream(message);
            ObjectInputStream objectStream = new ObjectInputStream(byteStream);
            Object packet = objectStream.readObject();
            List<Consumer<?>> packetListeners = listeners.get(packet.getClass());
            if (packetListeners != null) {
                for (Consumer listener : packetListeners) {
                    listener.accept(packet);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            plugin.getLogger().severe("Failed to receive packet: " + e.getMessage());
        }
    }

    public void shutdown() {
        for (String channel : new ArrayList<>(registeredChannels)) {
            unregisterChannel(channel);
        }
    }
}
