package com.example.mvc1.controllers;

import com.example.mvc1.dtos.Views;
import com.example.mvc1.dtos.user.UserRequest;
import com.example.mvc1.dtos.user.UserResponse;
import com.example.mvc1.services.UserService;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody UserRequest request) {
        return userService.create(request);
    }

    @JsonView(Views.UserFullWithOrders.class)
    @GetMapping("/{id}]")
    @ResponseStatus(HttpStatus.OK)
    public UserResponse getOneWithOrders(@PathVariable("id") Long userId) {
        return userService.getOneWithOrders(userId);
    }

    @JsonView(Views.UserFull.class)
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<UserResponse> getListWithPagination(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC)
            Pageable pageable
    ){
        return userService.getListWithPagination(pageable);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserResponse update(@PathVariable("id") Long userId, @Valid @RequestBody UserRequest request) {
        return userService.update(userId, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void softDelete(@PathVariable("id") Long userId) {
        userService.softDelete(userId);
    }
}
