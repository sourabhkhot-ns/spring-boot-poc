package com.example.employee_management_service.service;

import com.example.employee_management_service.model.Employee;
import java.util.List;

public interface EmployeeService {
    Employee createEmployee(Employee employee);
    List<Employee> getAllEmployees();
    Employee getEmployeeById(Long id);
    void deleteEmployee(Long id);
}
