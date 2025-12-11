package com.example.modloader.api.network;

import java.io.Serializable;

public class VoicePacket extends Packet implements Serializable {
    private static final long serialVersionUID = 1L;
    private final byte[] data;

    public VoicePacket(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }
}

