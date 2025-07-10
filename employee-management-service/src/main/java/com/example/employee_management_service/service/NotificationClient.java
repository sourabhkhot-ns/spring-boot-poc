package com.example.employee_management_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;

@Component
public class NotificationClient {
    @Value("${NOTIFICATION_URL:http://localhost:8080}")
    private String notificationUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendNotification(String message, Long employeeId) {
        try {
            String url = notificationUrl + "/api/notifications";
            var payload = new HashMap<String, Object>();
            payload.put("message", message);
            payload.put("employeeId", employeeId);
            restTemplate.postForObject(url, payload, Void.class);
        } catch (Exception ignored) {}
    }
} 