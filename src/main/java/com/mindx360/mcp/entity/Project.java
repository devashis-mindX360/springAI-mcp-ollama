package com.mindx360.mcp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "projects")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer projectId;

    @Column(nullable = false, length = 100)
    private String name;

    private LocalDate startDate;
    private LocalDate endDate;

    // Maps to FOREIGN KEY (client_id)
    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;
}