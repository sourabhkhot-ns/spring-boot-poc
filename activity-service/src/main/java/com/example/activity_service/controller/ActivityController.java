package com.example.activity_service.controller;

import com.example.activity_service.model.Activity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {
    private static final Logger logger = LoggerFactory.getLogger(ActivityController.class);
    private static final String ACTIVITY_FILE = "activities.json";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping
    public void createActivity(@RequestBody Activity activity) {
        logger.info("Recording activity: {} from {}", activity.getType(), activity.getService());
        List<Activity> activities = readActivities();
        activities.add(activity);
        writeActivities(activities);
    }

    @GetMapping
    public List<Activity> getAllActivities() {
        logger.info("Fetching all activities");
        return readActivities();
    }

    private List<Activity> readActivities() {
        try {
            File file = new File(ACTIVITY_FILE);
            if (!file.exists()) return new ArrayList<>();
            List<Activity> activities = objectMapper.readValue(file, new TypeReference<List<Activity>>() {});
            logger.debug("Read {} activities from file", activities.size());
            return activities;
        } catch (IOException e) {
            logger.error("Failed to read activities from file", e);
            throw new RuntimeException(e);
        }
    }

    private void writeActivities(List<Activity> activities) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(ACTIVITY_FILE), activities);
            logger.debug("Wrote {} activities to file", activities.size());
        } catch (IOException e) {
            logger.error("Failed to write activities to file", e);
            throw new RuntimeException(e);
        }
    }
} 