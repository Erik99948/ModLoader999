package com.example.modloader.api;

import java.util.UUID;

@FunctionalInterface
public interface VoiceDataListener {
    void onVoiceData(byte[] data, UUID sourcePlayerId);
}
