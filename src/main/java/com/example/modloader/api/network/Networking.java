package com.example.modloader.api.network;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class Networking implements PluginMessageListener {

    private final JavaPlugin plugin;
    private final String channel = "modloader:main";
    private final Map<Class<? extends Packet>, List<Consumer<? extends Packet>>> packetListeners = new HashMap<>();

    public Networking(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, channel, this);
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, channel);
    }

    public <T extends Packet> void registerListener(Class<T> packetType, Consumer<T> listener) {
        packetListeners.computeIfAbsent(packetType, k -> new ArrayList<>()).add(listener);
    }

    public void sendPacket(Player player, Packet packet) {
        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
            objectStream.writeObject(packet);
            objectStream.flush();
            player.sendPluginMessage(plugin, channel, byteStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (channel.equals(this.channel)) {
            try {
                ByteArrayInputStream byteStream = new ByteArrayInputStream(message);
                ObjectInputStream objectStream = new ObjectInputStream(byteStream);
                Packet packet = (Packet) objectStream.readObject();
                List<Consumer<? extends Packet>> listeners = packetListeners.get(packet.getClass());
                if (listeners != null) {
                    for (Consumer listener : listeners) {
                        listener.accept(packet);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
