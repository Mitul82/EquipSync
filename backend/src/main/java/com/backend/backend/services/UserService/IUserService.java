package com.backend.backend.services.UserService;

import java.util.List;
import java.util.UUID;

import com.backend.backend.dto.UserDTO;
import com.backend.backend.enums.ERoles;

public interface IUserService {
    List<UserDTO> getAllUsers();
    
    UserDTO getCurrentUser(UUID userId);

    UserDTO assignRoleToUser(UUID userId, ERoles roleEnum);
}