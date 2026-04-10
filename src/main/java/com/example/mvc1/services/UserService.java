package com.example.mvc1.services;

import com.example.mvc1.dtos.user.UserRequest;
import com.example.mvc1.dtos.user.UserResponse;
import com.example.mvc1.entities.User;
import com.example.mvc1.exceptions.ConflictException;
import com.example.mvc1.exceptions.ResourceNotFoundException;
import com.example.mvc1.mappers.UserMapper;
import com.example.mvc1.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final OrderService orderService;

    @Transactional
    public UserResponse create(UserRequest request) {
        User user = userMapper.toEntity(request);
        User saved = userRepository.save(user);
        return userMapper.toResponse(saved);
    }

    public UserResponse getOneWithOrders(Long userId) {
        User user = userRepository.findActiveById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User not found with id: " + userId));
        UserResponse response = userMapper.toResponse(user);
        response.setOrders(orderService.getList(userId));
        return response;
    }

    public Page<UserResponse> getListWithPagination(Long userId, Pageable pageable) {
        Page<User> usersPage = userRepository.findAllActivePaginated(userId, pageable);
        return usersPage.map(userMapper::toResponse);
    }

    public UserResponse update(Long userId, UserRequest request) {
        User user = userRepository.findActiveById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User not found with id: " + userId));
        userMapper.update(request, user);
        User saved = userRepository.save(user);
        return userMapper.toResponse(saved);
    }

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
