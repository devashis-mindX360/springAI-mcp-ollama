package com.mindx360.mcp.repository;

import com.mindx360.mcp.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    // That's it! You now have save(), findById(), findAll(), deleteById()

    List<Employee> findByStatus(Employee.Status status);

    List<Employee> findByDepartment_Name(String name);

    List<Employee> findByFirstNameContainingIgnoreCase(String keyword);

    List<Employee> findByHireDateAfter(LocalDate date);
}