package com.mindx360.mcp.service;

import com.mindx360.mcp.entity.Department;
import com.mindx360.mcp.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {
    private final DepartmentRepository repo;

    public List<Department> getAllDepartments() {
        return repo.findAll();
    }
}