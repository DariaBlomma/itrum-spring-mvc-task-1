package com.example.mvc1.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @Column(nullable = false, length = 200)
    private String userName;

    @Column(length = 200, nullable = false, unique = true)
    private String email;

    private String color;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Order> orders = new ArrayList<>();

    private Instant deletedAt;

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
