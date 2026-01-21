package com.example.modloader.api;

import java.util.UUID;

/**
 * Listener for voice data received events.
 */
@FunctionalInterface
public interface VoiceDataListener {
    void onVoiceData(byte[] data, UUID sourcePlayerId);
}
