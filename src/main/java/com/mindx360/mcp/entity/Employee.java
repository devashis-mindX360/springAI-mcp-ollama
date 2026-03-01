package com.mindx360.mcp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "employees")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer employeeId;

    @Column(nullable = false)
    private String firstName;
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    private String phone;
    private String jobTitle;
    private LocalDate hireDate;

    @Enumerated(EnumType.STRING) // Saves 'Active'/'Inactive' as strings in DB
    private Status status = Status.Active;

    // The Foreign Key Relationship
    @ManyToOne 
    @JoinColumn(name = "department_id") // Links to Department's ID
    private Department department;

    public enum Status {
        Active, Inactive
    }
}