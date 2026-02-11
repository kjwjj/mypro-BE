package com.example.backend.controller;

import com.example.backend.dto.CreateUserRequest;
import com.example.backend.entity.User;
import com.example.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService service;

    @GetMapping
    public List<User> list() {
        return service.findAll();
    }

    @PostMapping
    public User create(@RequestBody CreateUserRequest request) {
        return service.create(request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
