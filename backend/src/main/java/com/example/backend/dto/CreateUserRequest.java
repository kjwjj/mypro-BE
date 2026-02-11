package com.example.backend.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CreateUserRequest {
    private String name;
    private String city;
}
