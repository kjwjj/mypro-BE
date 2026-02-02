package com.example.mypro_be.user.repository;

import com.example.mypro_be.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    User findByEmail(String email);             // 로그인 시 이메일 조회
}
