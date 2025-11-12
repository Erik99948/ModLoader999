package com.example.modloader.api;

import java.util.UUID;

/**
 * Functional interface for listening to incoming voice data.
 */
@FunctionalInterface
public interface VoiceDataListener {
    /**
     * Called when voice data is received from another player.
     *
     * @param data The raw audio data (e.g., PCM bytes).
     * @param sourcePlayerId The UUID of the player who sent the voice data.
     */
    void onVoiceData(byte[] data, UUID sourcePlayerId);
}
