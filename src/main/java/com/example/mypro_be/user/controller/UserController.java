package com.example.mypro_be.user.controller;

import com.example.mypro_be.user.entity.User;
import com.example.mypro_be.user.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3000") // React와 연결
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ---------------- 회원가입 ----------------
    @PostMapping("/signup/form")
    public User signUp(@RequestBody User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }
        return userRepository.save(user);
    }

    // ---------------- 로그인 ----------------
    @PostMapping("/login") // 프론트에서 fetch할 URL: /api/users/login
    public ResponseEntity<?> login(@RequestBody User loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("존재하지 않는 이메일입니다.");
        }

        if (!user.getPassword().equals(loginRequest.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("비밀번호가 올바르지 않습니다.");
        }

        return ResponseEntity.ok(user); // 로그인 성공 시 유저 정보 반환
    }
}
