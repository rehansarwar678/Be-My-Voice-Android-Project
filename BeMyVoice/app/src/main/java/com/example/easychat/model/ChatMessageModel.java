package com.example.easychat.model;

import com.google.firebase.Timestamp;

public class ChatMessageModel {
    public String massageType;
    private String message;
    private String senderId;
    private Timestamp timestamp;

    public ChatMessageModel() {
    }

    public ChatMessageModel(String type, String message, String senderId, Timestamp timestamp) {
        this.massageType = type;
        this.message = message;
        this.senderId = senderId;
        this.timestamp = timestamp;
    }

    public String getMassageType() {
        return massageType;
    }

    public void setMassageType(String massageType) {
        this.massageType = massageType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
