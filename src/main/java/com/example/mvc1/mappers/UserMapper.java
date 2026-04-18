package com.example.mvc1.mappers;

import com.example.mvc1.dtos.user.UserRequest;
import com.example.mvc1.dtos.user.UserResponse;
import com.example.mvc1.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toEntity(UserRequest request);

    @Mapping(target = "orders", ignore = true)
    UserResponse toResponse(User user);

    void update(UserRequest request, @MappingTarget User user);
}
