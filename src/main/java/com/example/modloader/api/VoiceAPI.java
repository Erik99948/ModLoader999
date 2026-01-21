package com.example.modloader.api;

import java.util.UUID;

/**
 * API for voice communication.
 */
public interface VoiceAPI {
    void startVoiceCapture();
    void stopVoiceCapture();
    void sendVoiceData(byte[] data, UUID targetPlayerId);
    void onVoiceDataReceived(VoiceDataListener listener);
    void playVoiceData(byte[] data, UUID sourcePlayerId);
}
