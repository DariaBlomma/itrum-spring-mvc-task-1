package com.example.mvc1.controllers;

import com.example.mvc1.dtos.order.OrderRequest;
import com.example.mvc1.dtos.order.OrderResponse;
import com.example.mvc1.entities.Order;
import com.example.mvc1.services.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(Long userId, @Valid @RequestBody OrderRequest request) {
        return orderService.create(userId, request);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public OrderResponse getOne(Long userId, @PathVariable("id") Long orderId) {
        return orderService.getOne(userId, orderId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<OrderResponse> getList(Long userId) {
        return orderService.getList(userId);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public OrderResponse update(Long userId, @PathVariable("id") Long orderId, @Valid @RequestBody  OrderRequest request) {
        return orderService.update(userId, orderId, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSoft(Long userId, Long orderId) {
        orderService.deleteSoft(userId, orderId);
    }
}
