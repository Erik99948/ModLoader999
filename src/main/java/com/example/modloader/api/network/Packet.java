package com.example.modloader.api.network;

import org.bukkit.entity.Player;

import java.io.Serializable;
import java.util.UUID;

public abstract class Packet implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID senderId;
    private transient Player sender;

    public Packet() {
    }

    public UUID getSenderId() {
        return senderId;
    }

    public void setSender(Player sender) {
        this.sender = sender;
        this.senderId = sender.getUniqueId();
    }

    public Player getSender() {
        return sender;
    }
}
