package com.backend.backend.services.AuthService;

import com.backend.backend.dto.UserDTO;

public interface IAuthService {
    UserDTO signup(String email, String password, String department);

    UserDTO login(String email, String password);
}