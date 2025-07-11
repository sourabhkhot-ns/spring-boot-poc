package com.example.employee_management_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.time.Instant;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ActivityClient {
    private static final Logger logger = LoggerFactory.getLogger(ActivityClient.class);

    @Value("${ACTIVITY_URL:http://localhost:8083}")
    private String activityUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendActivity(String type, Object details) {
        try {
            String url = activityUrl + "/api/activities";
            var payload = new HashMap<String, Object>();
            payload.put("timestamp", Instant.now().toString());
            payload.put("service", "employee-management-service");
            payload.put("type", type);
            payload.put("details", details);
            logger.info("Sending activity: {} from {} to {}", type, "employee-management-service", url);
            restTemplate.postForObject(url, payload, Void.class);
        } catch (Exception e) {
            logger.error("Failed to send activity event: {}", type, e);
        }
    }
} 