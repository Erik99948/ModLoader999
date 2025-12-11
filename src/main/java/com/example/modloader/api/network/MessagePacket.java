package com.example.modloader.api.network;

public class MessagePacket extends Packet {
    private static final long serialVersionUID = 1L;

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

    public String getSenderModId() {
        return senderModId;
    }

    public String getRecipientModId() {
        return recipientModId;
    }

    public String getMessageType() {
        return messageType;
    }

    public String getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return "MessagePacket{" +
               "senderModId='" + senderModId + "'" +
               ", recipientModId='" + recipientModId + "'" +
               ", messageType='" + messageType + "'" +
               ", payload='" + payload + "'" +
               '}';
    }
}

