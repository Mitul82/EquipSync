package com.backend.backend.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.backend.dto.UserDTO;
import com.backend.backend.enums.ERoles;
import com.backend.backend.responses.ApiResponse;
import com.backend.backend.services.UserService.IUserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/user")
public class UserControllers {
    private final IUserService userService;

    @GetMapping("/get-current")
    public ResponseEntity<ApiResponse> getCurrentUser() {
        UserDTO user = userService.getCurrentUser();

        return ResponseEntity.ok().body(new ApiResponse("", user));
    }
    
    @GetMapping("/get-all")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<ApiResponse> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();

        return ResponseEntity.ok().body(new ApiResponse("Retreived all users", users));
    }
    
    @PatchMapping("/update/{userId}")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<ApiResponse> updateUserRole(@PathVariable UUID userId, @RequestParam ERoles role) {
        UserDTO user = userService.assignRoleToUser(userId, role);

        return ResponseEntity.ok().body(new ApiResponse("Updated user role successfully", user));
    }
}