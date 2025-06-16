package com.orderservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "partner")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Partner {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "credit_limit", nullable = false)
    private BigDecimal creditLimit;

}
