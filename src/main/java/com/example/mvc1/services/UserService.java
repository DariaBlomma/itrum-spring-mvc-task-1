package com.example.mvc1.services;

import com.example.mvc1.dtos.order.OrderResponse;
import com.example.mvc1.dtos.user.UserRequest;
import com.example.mvc1.dtos.user.UserResponse;
import com.example.mvc1.entities.User;
import com.example.mvc1.exceptions.ConflictException;
import com.example.mvc1.exceptions.ResourceNotFoundException;
import com.example.mvc1.mappers.OrderMapper;
import com.example.mvc1.mappers.UserMapper;
import com.example.mvc1.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final OrderService orderService;
    private final OrderMapper orderMapper;

    public UserResponse create(UserRequest request) {
        User user = userMapper.toEntity(request);
        User saved = userRepository.save(user);
        UserResponse response = userMapper.toResponse(saved);
        response.setOrders(new ArrayList<>());
        return response;
    }

    public UserResponse getOneWithOrders(Long userId) {
        User user = userRepository.findActiveById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User not found with id: " + userId));
        UserResponse response = userMapper.toResponse(user);
        response.setOrders(orderService.getList(userId));
        return response;
    }

    public Page<UserResponse> getListWithPagination(Pageable pageable) {
        Page<User> usersPage = userRepository.findAllActiveWithOrdersPaginated(pageable);
        return usersPage.map(user -> {
            UserResponse response = userMapper.toResponse(user);
            if (user.getOrders() == null) {
                response.setOrders(new ArrayList<>());
            } else {
                List<OrderResponse> orders = user.getOrders().stream().map(orderMapper::toResponse).toList();
                response.setOrders(orders);
            }
            return response;
        });
    }

    @Transactional
    public UserResponse update(Long userId, UserRequest request) {
        User user = userRepository.findActiveById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User not found with id: " + userId));
        userMapper.update(request, user);
        User saved = userRepository.save(user);
        UserResponse response = userMapper.toResponse(saved);
        List<OrderResponse> orders = orderService.getList(userId);
        response.setOrders(orders);
        return response;
    }

    public void softDelete(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User not found with id: " + userId));
        if (user.isDeleted()) {
            throw new ConflictException("User is already deleted");
        }

        user.setDeletedAt(Instant.now());
    }
}
