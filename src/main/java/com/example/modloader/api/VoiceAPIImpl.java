package com.example.modloader.api;

import com.example.modloader.api.dependencyinjection.Singleton;
import com.example.modloader.api.network.Networking;
import com.example.modloader.api.network.VoicePacket;
import org.bukkit.Bukkit;
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
    private TargetDataLine targetDataLine;
    private SourceDataLine sourceDataLine;

    private Thread captureThread;
    private volatile boolean running;

    public VoiceAPIImpl(Networking networking) {
        this.networking = networking;
        this.audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100.0F, 16, 1, 2, 44100.0F, false);

        this.networking.registerListener(VOICE_CHANNEL, VoicePacket.class, voicePacket -> {
            byte[] data = voicePacket.getData();
            UUID sourcePlayerId = voicePacket.getSenderId();
            for (VoiceDataListener listener : listeners) {
                listener.onVoiceData(data, sourcePlayerId);
            }
            playVoiceData(data, sourcePlayerId);
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
                byte[] buffer = new byte[targetDataLine.getBufferSize() / 5];
                int bytesRead;
                while (running && (bytesRead = targetDataLine.read(buffer, 0, buffer.length)) != -1) {
                    if (bytesRead > 0) {
                        byte[] voiceData = new byte[bytesRead];
                        System.arraycopy(buffer, 0, voiceData, 0, bytesRead);
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
                captureThread.join(1000);
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
        Player targetPlayer = Bukkit.getPlayer(targetPlayerId);
        if (targetPlayer != null) {
            networking.sendPacket(targetPlayer, VOICE_CHANNEL, new VoicePacket(data));
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

            sourceDataLine.write(data, 0, data.length);
        } catch (LineUnavailableException e) {
            LOGGER.log(Level.SEVERE, "Failed to play voice data: Line unavailable.", e);
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.SEVERE, "Failed to play voice data: Illegal argument (e.g., invalid audio format).", e);
        } catch (SecurityException e) {
            LOGGER.log(Level.SEVERE, "Failed to play voice data: Security exception (speaker access denied?).", e);
        }
    }

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
