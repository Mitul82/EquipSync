package com.backend.backend.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class UserDTO {
    private UUID id;

    private String email;

    private String department;

    private String role;
}