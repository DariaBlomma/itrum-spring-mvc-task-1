package com.example.mvc1.services;

import com.example.mvc1.entities.User;
import com.example.mvc1.exceptions.ConflictException;
import com.example.mvc1.exceptions.ResourceNotFoundException;
import com.example.mvc1.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public void softDelete(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User not found with id: " + userId));
        if (user.isDeleted()) {
            throw  new ConflictException("User is already deleted");
        }

        user.setDeletedAt(Instant.now());
    }
}
