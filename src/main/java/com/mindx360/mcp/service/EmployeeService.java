package com.mindx360.mcp.service;

import com.mindx360.mcp.entity.Employee;
import com.mindx360.mcp.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Business-logic layer for Employee operations.
 *
 * <p>All repository access is funnelled through this service so that:
 * <ul>
 *   <li>Transaction boundaries are clearly defined.</li>
 *   <li>The {@code EmployeeTools} class stays free of persistence concerns.</li>
 *   <li>Migrating to MySQL requires zero changes here (only properties).</li>
 * </ul>
 *
 * <p>Constructor injection is used exclusively (SOLID, no field injection).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)

public class EmployeeService {

    private final EmployeeRepository repo;

    public List<Employee> getAllEmployees() {
        return repo.findAll();
    }

    public Employee getEmployeeById(Integer id) {
        return repo.findById(id).orElseThrow();
    }

    public List<Employee> getEmployeesByDepartment(String dept) {
        return repo.findByDepartment_Name(dept);
    }

    public List<Employee> getEmployeesByStatus(String status) {
        return repo.findByStatus(Employee.Status.valueOf(status));
    }

    public List<Employee> searchEmployees(String keyword) {
        return repo.findByFirstNameContainingIgnoreCase(keyword);
    }

    public List<Employee> hiredAfter(String date) {
        return repo.findByHireDateAfter(LocalDate.parse(date));
    }

    public long getEmployeeCount() {
        return repo.count();
    }

}
