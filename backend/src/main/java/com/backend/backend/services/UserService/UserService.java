package com.backend.backend.services.UserService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.backend.dto.UserDTO;
import com.backend.backend.enums.ERoles;
import com.backend.backend.exceptions.ResourceNotFound;
import com.backend.backend.models.RoleModel;
import com.backend.backend.models.UserModel;
import com.backend.backend.repository.RolesRepository;
import com.backend.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final ModelMapper mapper;
    private final UserRepository repo;
    private final RolesRepository roleRepo;
    
    @Override
    @Transactional
    public UserDTO assignRoleToUser(UUID userId, ERoles roleEnum) {
        UserModel user = repo.findById(userId).orElseThrow(() -> new ResourceNotFound("User not found"));

        RoleModel role = roleRepo.findByRole(roleEnum).orElseThrow(() -> new ResourceNotFound("Role " + roleEnum + " is not assignable"));

        user.setRole(role);

        UserModel updateUser = repo.save(user);

        UserDTO dto = mapper.map(updateUser, UserDTO.class);

        dto.setRole(updateUser.getRole().getRole().name());

        return dto;
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return repo.findAll().stream().map(user -> mapper.map(user, UserDTO.class)).collect(Collectors.toList());
    }

    @Override
    public UserDTO getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        UserModel user = repo.findByEmailIgnoreCase(email).orElseThrow(() -> new ResourceNotFound("Current user not found in database"));

        UserDTO dto = mapper.map(user, UserDTO.class);

        dto.setRole(user.getRole().getRole().name());

        return dto;
    }
}