package com.hekmatullahamin.click.utils;

public class Message {
    private String message;
    private String time;
    private String date;
    private String id;
    private String type;
    private String senderId;

    public Message() {
    }

    public Message(String message, String time, String date, String id, String type, String senderId) {
        this.message = message;
        this.time = time;
        this.date = date;
        this.id = id;
        this.type = type;
        this.senderId = senderId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }
}
