package com.example.mvc1.mappers;

import com.example.mvc1.dtos.user.UserCreateRequest;
import com.example.mvc1.dtos.user.UserResponse;
import com.example.mvc1.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toEntity(UserCreateRequest request);

    @Mapping(target = "orders", ignore = true)
    UserResponse toResponse(User user);
}
