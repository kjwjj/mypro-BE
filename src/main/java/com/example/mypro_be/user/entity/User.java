package com.example.mypro_be.user.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDate birth;

    @Column(nullable = false)
    private String gender; // "M" 또는 "F"

    @Column(nullable = false)
    private String phone;

    private LocalDateTime createdAt = LocalDateTime.now();

    // 기본 생성자
    public User() {}

    // 생성자
    public User(String email, String password, String name, LocalDate birth, String gender, String phone) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.birth = birth;
        this.gender = gender;
        this.phone = phone;
    }

    // getters & setters 생략
}
