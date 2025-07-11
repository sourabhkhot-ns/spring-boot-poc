package com.example.activity_service.model;

public class Activity {
    private String timestamp;
    private String service;
    private String type;
    private Object details;

    // Getters and setters
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public String getService() { return service; }
    public void setService(String service) { this.service = service; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Object getDetails() { return details; }
    public void setDetails(Object details) { this.details = details; }
} 