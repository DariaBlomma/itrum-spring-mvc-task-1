package com.example.mvc1.mappers;

import com.example.mvc1.dtos.order.OrderRequest;
import com.example.mvc1.dtos.order.OrderResponse;
import com.example.mvc1.entities.Order;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    Order toEntity(OrderRequest request);

    OrderResponse toResponse(Order order);

    void update(OrderRequest request, @MappingTarget Order order);
}
