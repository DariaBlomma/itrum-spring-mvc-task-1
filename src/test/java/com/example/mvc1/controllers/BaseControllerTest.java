package com.example.mvc1.controllers;

import com.example.mvc1.dtos.user.UserResponse;
import com.example.mvc1.entities.User;
import com.example.mvc1.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;
import static org.mockito.Mockito.when;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class BaseControllerTest {
    @Autowired
    protected MockMvcTester mockMvcTester;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoBean
    protected UserRepository userRepository;

    protected final Long userId = 1L;

    protected User getTestUser() {
        return new User(userId, "testUser", "test@gmail.com", "#FFF", List.of(), null);
    }

    protected UserResponse getTestUserResponse() {
        return new UserResponse(userId, "testUser", "test@gmail.com", List.of(), "#FFF", null);
    }

    protected void mockNotDeletedUserFound() {
        when(userRepository.findActiveById(userId)).thenReturn(Optional.of(getTestUser()));
    }

    protected void mockNotDeletedUserNotFound() {
        when(userRepository.findActiveById(userId)).thenReturn(Optional.empty());
    }
}
