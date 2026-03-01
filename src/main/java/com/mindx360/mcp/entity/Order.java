package com.mindx360.mcp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "orders")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderId;

    private LocalDate orderDate;

    // Precision 10, Scale 2 matches your DECIMAL(10,2)
    @Column(precision = 10, scale = 2)
    private BigDecimal totalAmount;

    // Maps to FOREIGN KEY (client_id)
    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;
}