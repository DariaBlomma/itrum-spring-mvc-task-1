package com.example.mvc1.dtos.user;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class UserRequest {
    @NotNull(message = "Login is required")
    @Size(min = 2, max = 200, message = "User Name must be between 2 and 200 characters")
    private String userName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$",
            message = "Color must be valid hex format (#RGB or #RRGGBB)")
    private String color;
}
