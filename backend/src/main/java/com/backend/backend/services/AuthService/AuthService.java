package com.backend.backend.services.AuthService;

import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.backend.backend.dto.UserDTO;
import com.backend.backend.enums.ERoles;
import com.backend.backend.models.RoleModel;
import com.backend.backend.models.UserModel;
import com.backend.backend.repository.RolesRepository;
import com.backend.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {
    private final ModelMapper mapper;
    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final RolesRepository rolesRepo;

    @Override
    public UserDTO login(String email, String password) {
        UserModel user = repo.findByEmailIgnoreCase(email).orElseThrow(() -> new BadCredentialsException("Invalid Email or password"));

        if(!encoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Invalid Email or Password");
        }

        UserDTO dto = mapper.map(user, UserDTO.class);

        dto.setRole(user.getRole().getRole().name());

        return dto;
    }

    @Override
    public UserDTO signup(String email, String password, String department) {
        RoleModel employeeRole = rolesRepo.findByRole(ERoles.Employee).orElseThrow(() -> new IllegalStateException("Default role is missing"));
        UserModel user = new UserModel();

        user.setEmail(email);
        user.setPassword(encoder.encode(password));
        user.setDepartment(department);
        user.setRole(employeeRole);

        UserModel savedUser = repo.save(user);

        UserDTO dto = mapper.map(savedUser, UserDTO.class);

        dto.setRole(user.getRole().getRole().name());

        return dto;
    }
}