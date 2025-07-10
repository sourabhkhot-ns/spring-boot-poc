package com.example.employee_management_service.service;

import com.example.employee_management_service.model.Employee;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class EmployeeServiceImpl implements EmployeeService {
    private static final String EMPLOYEE_FILE = "employees.json";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private NotificationClient notificationClient;

    @Value("${NOTIFICATION_URL:http://localhost:8080}")
    private String notificationUrl;

    private List<Employee> readEmployees() {
        try {
            File file = new File(EMPLOYEE_FILE);
            if (!file.exists()) return new ArrayList<>();
            return objectMapper.readValue(file, new TypeReference<List<Employee>>() {});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeEmployees(List<Employee> employees) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(EMPLOYEE_FILE), employees);
        } catch (IOException e) {
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
        notificationClient.sendNotification("Employee Created", employee.getId());
        return employee;
    }

    @Override
    public List<Employee> getAllEmployees() {
        return readEmployees();
    }

    @Override
    public Employee getEmployeeById(Long id) {
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
            notificationClient.sendNotification("Employee Updated", id);
            return emp;
        }
        return null;
    }

    @Override
    public void deleteEmployee(Long id) {
        List<Employee> employees = readEmployees();
        boolean removed = employees.removeIf(e -> e.getId().equals(id));
        writeEmployees(employees);
        if (removed) notificationClient.sendNotification("Employee Deleted", id);
    }
} 