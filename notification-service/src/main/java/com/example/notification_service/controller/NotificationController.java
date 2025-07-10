package com.example.notification_service.controller;

import com.example.notification_service.model.NotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);
    private static final String NOTIF_FILE = "notifications.json";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping
    public void receiveNotification(@RequestBody NotificationRequest notification) {
        logger.info("\uD83D\uDD14 Notification received: {} for Employee ID: {}", notification.getMessage(), notification.getEmployeeId());
        List<NotificationRequest> notifications = readNotifications();
        notifications.add(notification);
        writeNotifications(notifications);
    }

    @GetMapping
    public List<NotificationRequest> getAllNotifications() {
        logger.info("Fetching all notifications");
        return readNotifications();
    }

    private List<NotificationRequest> readNotifications() {
        try {
            File file = new File(NOTIF_FILE);
            if (!file.exists()) return new ArrayList<>();
            List<NotificationRequest> notifications = objectMapper.readValue(file, new TypeReference<List<NotificationRequest>>() {});
            logger.debug("Read {} notifications from file", notifications.size());
            return notifications;
        } catch (IOException e) {
            logger.error("Failed to read notifications from file", e);
            throw new RuntimeException(e);
        }
    }

    private void writeNotifications(List<NotificationRequest> notifications) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(NOTIF_FILE), notifications);
            logger.debug("Wrote {} notifications to file", notifications.size());
        } catch (IOException e) {
            logger.error("Failed to write notifications to file", e);
            throw new RuntimeException(e);
        }
    }
}
