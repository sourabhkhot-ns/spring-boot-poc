package com.example.employee_management_service.service;

import com.example.employee_management_service.model.Employee;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmployeeServiceImpl implements EmployeeService {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeServiceImpl.class);
    private static final String EMPLOYEE_FILE = "employees.json";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private NotificationClient notificationClient;

    @Autowired
    private ActivityClient activityClient;

    private List<Employee> readEmployees() {
        try {
            File file = new File(EMPLOYEE_FILE);
            if (!file.exists()) return new ArrayList<>();
            return objectMapper.readValue(file, new TypeReference<List<Employee>>() {});
        } catch (IOException e) {
            logger.error("Failed to read employees from file", e);
            throw new RuntimeException(e);
        }
    }

    private void writeEmployees(List<Employee> employees) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(EMPLOYEE_FILE), employees);
        } catch (IOException e) {
            logger.error("Failed to write employees to file", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Employee createEmployee(Employee employee) {
        List<Employee> employees = readEmployees();
        long newId = employees.stream().mapToLong(e -> e.getId() != null ? e.getId() : 0).max().orElse(0) + 1;
        employee.setId(newId);
        employees.add(employee);
        writeEmployees(employees);
        logger.info("Employee created: {} {} (ID: {})", employee.getFirstName(), employee.getLastName(), employee.getId());
        notificationClient.sendNotification("Employee Created", employee.getId());
        activityClient.sendActivity("Employee Created", employee);
        return employee;
    }

    @Override
    public List<Employee> getAllEmployees() {
        logger.info("Returning all employees");
        return readEmployees();
    }

    @Override
    public Employee getEmployeeById(Long id) {
        logger.info("Getting employee by id: {}", id);
        return readEmployees().stream().filter(e -> e.getId().equals(id)).findFirst().orElse(null);
    }

    @Override
    public Employee updateEmployee(Long id, Employee updated) {
        List<Employee> employees = readEmployees();
        Optional<Employee> opt = employees.stream().filter(e -> e.getId().equals(id)).findFirst();
        if (opt.isPresent()) {
            Employee emp = opt.get();
            emp.setFirstName(updated.getFirstName());
            emp.setLastName(updated.getLastName());
            emp.setEmail(updated.getEmail());
            writeEmployees(employees);
            logger.info("Employee updated: {} (ID: {})", emp.getFirstName(), id);
            notificationClient.sendNotification("Employee Updated", id);
            activityClient.sendActivity("Employee Updated", emp);
            return emp;
        }
        logger.warn("Employee not found for update: id {}", id);
        return null;
    }

    @Override
    public void deleteEmployee(Long id) {
        List<Employee> employees = readEmployees();
        boolean removed = employees.removeIf(e -> e.getId().equals(id));
        writeEmployees(employees);
        if (removed) {
            logger.info("Employee deleted: id {}", id);
            notificationClient.sendNotification("Employee Deleted", id);
            activityClient.sendActivity("Employee Deleted", id);
        } else {
            logger.warn("Employee not found for delete: id {}", id);
        }
    }
} 