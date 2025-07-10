package com.example.notification_service.controller;

import com.example.notification_service.model.NotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @PostMapping
    public void receiveNotification(@RequestBody NotificationRequest notification) {
        logger.info("\uD83D\uDD14 Notification received: {} for Employee ID: {}", notification.getMessage(), notification.getEmployeeId());
    }
}
