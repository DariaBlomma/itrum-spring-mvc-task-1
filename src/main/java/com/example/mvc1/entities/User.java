package com.example.mvc1.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import jakarta.validation.constraints.Email;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Table(name = "users")
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Login is required")
    @Column(nullable = false, length = 200)
    private String userName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Column(length = 200, nullable = false, unique = true)
    private String email;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Order> orders = new ArrayList<>();

    private Instant deletedAt;
}
