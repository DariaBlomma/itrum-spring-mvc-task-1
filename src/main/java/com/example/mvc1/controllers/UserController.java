package com.example.mvc1.controllers;

import com.example.mvc1.dtos.Views;
import com.example.mvc1.dtos.user.UserResponse;
import com.example.mvc1.services.UserService;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create() {

    }

    @JsonView(Views.UserFullWithOrders.class)
    @GetMapping("/{id}]")
    @ResponseStatus(HttpStatus.OK)
    public UserResponse getOneWithOrders(@PathVariable("id") Long userId) {

    }

    @JsonView(Views.UserFull.class)
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<UserResponse> User getList() {

    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserResponse update(@PathVariable("id") Long userId) {

    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void softDelete(@PathVariable("id") Long userId) {
        userService.softDelete(userId);
    }
}
