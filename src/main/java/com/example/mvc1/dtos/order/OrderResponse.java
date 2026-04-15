package com.example.mvc1.dtos.order;

import com.example.mvc1.dtos.Views;
import com.example.mvc1.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@AllArgsConstructor
@JsonView(Views.UserFullWithOrders.class)
public class OrderResponse {
    private Long id;
    private String title;
    private BigDecimal price;
    private OrderStatus status;
    private Instant deletedAt;
}
