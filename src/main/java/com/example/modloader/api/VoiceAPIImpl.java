package com.example.modloader.api;

import com.example.modloader.api.dependencyinjection.Singleton;
import com.example.modloader.api.network.Networking;
import com.example.modloader.api.network.PacketHandler;
import com.example.modloader.api.network.PacketType;
import org.bukkit.entity.Player;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class VoiceAPIImpl implements VoiceAPI {

    private static final Logger LOGGER = Logger.getLogger(VoiceAPIImpl.class.getName());
    private static final String VOICE_CHANNEL = "modloader:voice";

    private final Networking networking;
    private final List<VoiceDataListener> listeners = new CopyOnWriteArrayList<>();

    private AudioFormat audioFormat;
    private TargetDataLine targetDataLine; // For capturing audio
    private SourceDataLine sourceDataLine; // For playing audio

    private Thread captureThread;
    private volatile boolean running;

    public VoiceAPIImpl(Networking networking) {
        this.networking = networking;
        // Define a common audio format for voice chat
        // PCM, 16-bit, 44.1kHz, mono, signed, little-endian
        this.audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100.0F, 16, 1, 2, 44100.0F, false);

        // Register packet handler for incoming voice data
        this.networking.registerPacketHandler(VOICE_CHANNEL, new PacketHandler() {
            @Override
            public void handlePacket(Player player, byte[] data) {
                UUID sourcePlayerId = player.getUniqueId(); // Assuming the player sending the packet is the source
                for (VoiceDataListener listener : listeners) {
                    listener.onVoiceData(data, sourcePlayerId);
                }
                // Automatically play received voice data
                playVoiceData(data, sourcePlayerId);
            }
        });
        LOGGER.info("VoiceAPI initialized with Networking integration. Voice channel: " + VOICE_CHANNEL);
    }

    @Override
    public void startVoiceCapture() {
        if (running) {
            LOGGER.info("Voice capture is already running.");
            return;
        }

        try {
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
            if (!AudioSystem.isLineSupported(info)) {
                LOGGER.severe("TargetDataLine not supported for format: " + audioFormat);
                return;
            }
            targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
            targetDataLine.open(audioFormat);
            targetDataLine.start();

            running = true;
            captureThread = new Thread(() -> {
                byte[] buffer = new byte[targetDataLine.getBufferSize() / 5]; // Smaller buffer for lower latency
                int bytesRead;
                while (running && (bytesRead = targetDataLine.read(buffer, 0, buffer.length)) != -1) {
                    if (bytesRead > 0) {
                        // Create a copy of the data to avoid issues with buffer reuse
                        byte[] voiceData = new byte[bytesRead];
                        System.arraycopy(buffer, 0, voiceData, 0, bytesRead);
                        // In a real scenario, this would be sent to other players
                        // For now, we'll just log and potentially send via networking
                        // LOGGER.fine(String.format("Captured %d bytes of voice data.", bytesRead));

                        // Send captured data over the network to all connected players (or specific ones for proximity)
                        // The actual targetPlayerId logic would be handled by the mod using this API
                        // For demonstration, we'll send to a dummy UUID or rely on the mod to call sendVoiceData
                        // This part needs to be explicitly called by the mod developer using sendVoiceData(data, target)
                        // For now, we'll just make it available to listeners if any internal processing is needed
                        // No direct sending here, as sendVoiceData is the public API for that.
                    }
                }
                LOGGER.info("Voice capture thread stopped.");
            }, "VoiceCaptureThread");
            captureThread.setDaemon(true);
            captureThread.start();
            LOGGER.info("VoiceAPI: Started voice capture.");
        } catch (LineUnavailableException e) {
            LOGGER.log(Level.SEVERE, "Failed to start voice capture: Line unavailable.", e);
        } catch (SecurityException e) {
            LOGGER.log(Level.SEVERE, "Failed to start voice capture: Security exception (microphone access denied?).", e);
        }
    }

    @Override
    public void stopVoiceCapture() {
        if (!running) {
            LOGGER.info("Voice capture is not running.");
            return;
        }
        running = false;
        if (targetDataLine != null) {
            targetDataLine.stop();
            targetDataLine.close();
            targetDataLine = null;
        }
        if (captureThread != null) {
            try {
                captureThread.join(1000); // Wait for the thread to finish
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.log(Level.WARNING, "Interrupted while waiting for capture thread to stop.", e);
            }
            captureThread = null;
        }
        LOGGER.info("VoiceAPI: Stopped voice capture.");
    }

    @Override
    public void sendVoiceData(byte[] data, UUID targetPlayerId) {
        // Convert UUID to Player object (this would typically be done by the mod or a utility)
        // For now, we'll assume the Networking API can handle UUIDs or we need to find the player
        // This is a simplification; in a real scenario, you'd get the Player object from Bukkit
        Player targetPlayer = networking.getPlayer(targetPlayerId); // Assuming Networking has a way to get Player by UUID
        if (targetPlayer != null) {
            networking.sendPacket(targetPlayer, VOICE_CHANNEL, data, PacketType.UDP); // Assuming UDP for voice
            // LOGGER.fine(String.format("Sent %d bytes of voice data to %s via %s.", data.length, targetPlayerId, VOICE_CHANNEL));
        } else {
            LOGGER.warning("Could not find target player with UUID: " + targetPlayerId + " to send voice data.");
        }
    }

    @Override
    public void onVoiceDataReceived(VoiceDataListener listener) {
        listeners.add(listener);
        LOGGER.info("VoiceAPI: Registered a VoiceDataListener.");
    }

    @Override
    public void playVoiceData(byte[] data, UUID sourcePlayerId) {
        try {
            if (sourceDataLine == null || !sourceDataLine.isOpen()) {
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
                if (!AudioSystem.isLineSupported(info)) {
                    LOGGER.severe("SourceDataLine not supported for format: " + audioFormat);
                    return;
                }
                sourceDataLine = (SourceDataLine) AudioSystem.getLine(info);
                sourceDataLine.open(audioFormat);
                sourceDataLine.start();
            }

            // Play the audio data
            sourceDataLine.write(data, 0, data.length);
            // LOGGER.fine(String.format("Played %d bytes of voice data from %s.", data.length, sourcePlayerId));
        } catch (LineUnavailableException e) {
            LOGGER.log(Level.SEVERE, "Failed to play voice data: Line unavailable.", e);
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.SEVERE, "Failed to play voice data: Illegal argument (e.g., invalid audio format).", e);
        } catch (SecurityException e) {
            LOGGER.log(Level.SEVERE, "Failed to play voice data: Security exception (speaker access denied?).", e);
        }
    }

    // Ensure resources are cleaned up when the plugin disables
    public void shutdown() {
        stopVoiceCapture();
        if (sourceDataLine != null) {
            sourceDataLine.stop();
            sourceDataLine.close();
            sourceDataLine = null;
        }
        LOGGER.info("VoiceAPI shutdown complete.");
    }
}
