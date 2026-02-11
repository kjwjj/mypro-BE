package com.example.backend.service;

import com.example.backend.UserRepository;
import com.example.backend.dto.CreateUserRequest;
import com.example.backend.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;

    public List<User> findAll() {
        return repository.findAll();
    }

    public User create(CreateUserRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setCity(request.getCity());
        return repository.save(user);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}

