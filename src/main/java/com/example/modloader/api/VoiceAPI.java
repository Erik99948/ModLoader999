package com.example.modloader.api;

import java.util.UUID;

public interface VoiceAPI {
    /**
     * Starts capturing audio from the player's microphone.
     * The captured data will be made available to registered listeners or can be sent via sendVoiceData.
     */
    void startVoiceCapture();

    /**
     * Stops capturing audio from the player's microphone.
     */
    void stopVoiceCapture();

    /**
     * Sends captured voice data to a specific target player.
     *
     * @param data The raw audio data (e.g., PCM bytes).
     * @param targetPlayerId The UUID of the player to send the voice data to.
     */
    void sendVoiceData(byte[] data, UUID targetPlayerId);

    /**
     * Registers a listener to receive incoming voice data from other players.
     *
     * @param listener The listener to register.
     */
    void onVoiceDataReceived(VoiceDataListener listener);

    /**
     * Plays received voice data through the player's audio output.
     *
     * @param data The raw audio data (e.g., PCM bytes) to play.
     * @param sourcePlayerId The UUID of the player who sent the voice data.
     */
    void playVoiceData(byte[] data, UUID sourcePlayerId);
}
