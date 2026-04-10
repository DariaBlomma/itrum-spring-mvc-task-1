package com.example.mvc1.dtos.user;

import com.example.mvc1.dtos.Views;
import com.example.mvc1.dtos.order.OrderResponse;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String userName;
    private String email;

    @JsonView(Views.UserFullWithOrders.class)
    private List<OrderResponse> orders;

    @JsonView(Views.UserFull.class)
    private String color;

    private Instant deletedAt;
}
