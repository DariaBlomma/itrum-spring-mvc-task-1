package com.example.mvc1.services;

import com.example.mvc1.dtos.order.OrderRequest;
import com.example.mvc1.dtos.order.OrderResponse;
import com.example.mvc1.entities.Order;
import com.example.mvc1.entities.User;
import com.example.mvc1.exceptions.ConflictException;
import com.example.mvc1.exceptions.ResourceNotFoundException;
import com.example.mvc1.mappers.OrderMapper;
import com.example.mvc1.repositories.OrderRepository;
import com.example.mvc1.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final UserRepository userRepository;

    public OrderResponse create(Long userId, OrderRequest request) {
        User user = userRepository.findActiveById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User not found with id " + userId)
        );
        Order order = orderMapper.toEntity(request);
        order.setUser(user);
        Order saved = orderRepository.save(order);
        return orderMapper.toResponse(saved);
    }

    public OrderResponse getOne(Long userId, Long orderId) {
        Order order = orderRepository.findActiveByIdForUser(orderId, userId).orElseThrow(
                () -> new ResourceNotFoundException("Order not found with id " + orderId)
        );
        return orderMapper.toResponse(order);
    }

    public List<OrderResponse> getList(Long userId) {
        userRepository.findActiveById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User not found with id " + userId));
        List<Order> orders = orderRepository.findAllActiveForUser(userId);
        return orders.stream().map(orderMapper::toResponse).toList();
    }

    public OrderResponse update(Long userId, Long orderId, OrderRequest request) {
        Order order = orderRepository.findActiveByIdForUser(orderId, userId).orElseThrow(
                () -> new ResourceNotFoundException("Order not found with id " + orderId)
        );
        orderMapper.update(request, order);
        Order saved = orderRepository.save(order);
        return orderMapper.toResponse(saved);
    }

    public void deleteSoft(Long userId, Long orderId) {
        Order order = orderRepository.findByIdForUser(orderId, userId).orElseThrow(
                () -> new ResourceNotFoundException("Order not found with id " + orderId)
        );
        if (order.isDeleted()) {
            throw new ConflictException("Order is already deleted");
        }
        order.setDeletedAt(Instant.now());
    }
}
