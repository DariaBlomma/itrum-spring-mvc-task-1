package com.example.mvc1.controllers;

import com.example.mvc1.dtos.Views;
import com.example.mvc1.dtos.user.UserResponse;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create() {

    }

    @JsonView(Views.UserFullWithOrders.class)
    @GetMapping("/{id}]")
    @ResponseStatus(HttpStatus.OK)
    public UserResponse getOneWithOrders() {

    }

    @JsonView(Views.UserFull.class)
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<UserResponse> User getList() {

    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserResponse update() {

    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete() {

    }
}
