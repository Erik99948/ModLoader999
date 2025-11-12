package com.example.modloader.api.network;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;
import java.util.function.Consumer;

public class Networking implements PluginMessageListener {

    private final JavaPlugin plugin;
    private final Map<String, Map<Class<? extends Packet>, List<Consumer<? extends Packet>>>> channelPacketListeners = new ConcurrentHashMap<>();
    private final Set<String> registeredChannels = ConcurrentHashMap.newKeySet();

    public Networking(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerChannel(String channelName) {
        if (registeredChannels.add(channelName)) {
            plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, channelName, this);
            plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, channelName);
            plugin.getLogger().info("Registered network channel: " + channelName);
        }
    }

    public void unregisterChannel(String channelName) {
        if (registeredChannels.remove(channelName)) {
            plugin.getServer().getMessenger().unregisterIncomingPluginChannel(plugin, channelName, this);
            plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(plugin, channelName);
            channelPacketListeners.remove(channelName);
            plugin.getLogger().info("Unregistered network channel: " + channelName);
        }
    }

    public <T extends Packet> void registerListener(String channelName, Class<T> packetType, Consumer<T> listener) {
        if (!registeredChannels.contains(channelName)) {
            plugin.getLogger().warning("Attempted to register listener for unregistered channel: " + channelName + ". Registering channel now.");
            registerChannel(channelName);
        }
        channelPacketListeners.computeIfAbsent(channelName, k -> new ConcurrentHashMap<>())
                               .computeIfAbsent(packetType, k -> new ArrayList<>())
                               .add(listener);
    }

    public <T extends Packet> void unregisterListener(String channelName, Class<T> packetType, Consumer<T> listener) {
        Map<Class<? extends Packet>, List<Consumer<? extends Packet>>> packetListeners = channelPacketListeners.get(channelName);
        if (packetListeners != null) {
            List<Consumer<? extends Packet>> listeners = packetListeners.get(packetType);
            if (listeners != null) {
                listeners.remove(listener);
                if (listeners.isEmpty()) {
                    packetListeners.remove(packetType);
                }
            }
            if (packetListeners.isEmpty()) {
                channelPacketListeners.remove(channelName);
            }
        }
    }

    public void sendPacket(Player player, String channelName, Packet packet) {
        if (!registeredChannels.contains(channelName)) {
            plugin.getLogger().warning("Attempted to send packet on unregistered channel: " + channelName + ". Packet will not be sent.");
            return;
        }
        try {
            packet.setSender(player);
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
            objectStream.writeObject(packet);
            objectStream.flush();
            player.sendPluginMessage(plugin, channelName, byteStream.toByteArray());
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to send packet on channel " + channelName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendBungeeCordPacket(Player player, String subChannel, String serverName, Packet packet) {
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
            plugin.getLogger().severe("Failed to send BungeeCord packet on subchannel " + subChannel + " to server " + serverName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onPluginMessageReceived(String channelName, Player player, byte[] message) {
        Map<Class<? extends Packet>, List<Consumer<? extends Packet>>> packetListeners = channelPacketListeners.get(channelName);
        if (packetListeners == null) {
            return;
        }

        try {
            ByteArrayInputStream byteStream = new ByteArrayInputStream(message);
            ObjectInputStream objectStream = new ObjectInputStream(byteStream);
            Packet packet = (Packet) objectStream.readObject();
            packet.setSender(player);
            List<Consumer<? extends Packet>> listeners = packetListeners.get(packet.getClass());
            if (listeners != null) {
                for (Consumer listener : listeners) {
                    try {
                        listener.accept(packet);
                    } catch (ClassCastException e) {
                        plugin.getLogger().severe("Packet type mismatch for channel " + channelName + ". Expected " + packet.getClass().getName() + ", but listener expects different type.");
                        e.printStackTrace();
                    } catch (Exception e) {
                        plugin.getLogger().severe("Error dispatching packet on channel " + channelName + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            plugin.getLogger().severe("Failed to receive or deserialize packet on channel " + channelName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void shutdown() {
        for (String channelName : new ArrayList<>(registeredChannels)) {
            unregisterChannel(channelName);
        }
        channelPacketListeners.clear();
    }
}