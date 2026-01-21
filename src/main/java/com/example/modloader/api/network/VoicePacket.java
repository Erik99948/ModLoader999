package com.example.modloader.api.network;

import java.io.Serializable;
import java.util.UUID;

public class VoicePacket implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private UUID senderId;
    private final byte[] data;

    public VoicePacket(byte[] data) {
        this.data = data;
    }

    public UUID getSenderId() { return senderId; }
    public void setSenderId(UUID senderId) { this.senderId = senderId; }
    public byte[] getData() { return data; }
}
