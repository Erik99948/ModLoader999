package com.example.modloader.api.network;

import java.io.Serializable;
import java.util.UUID;

public class MessagePacket implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private UUID senderId;
    private final String senderModId;
    private final String recipientModId;
    private final String messageType;
    private final String payload;

    public MessagePacket(String senderModId, String recipientModId, String messageType, String payload) {
        this.senderModId = senderModId;
        this.recipientModId = recipientModId;
        this.messageType = messageType;
        this.payload = payload;
    }

    public UUID getSenderId() { return senderId; }
    public void setSenderId(UUID senderId) { this.senderId = senderId; }
    public String getSenderModId() { return senderModId; }
    public String getRecipientModId() { return recipientModId; }
    public String getMessageType() { return messageType; }
    public String getPayload() { return payload; }
}
