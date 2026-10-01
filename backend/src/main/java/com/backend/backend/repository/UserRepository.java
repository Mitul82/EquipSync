package com.backend.backend.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.backend.models.UserModel;

public interface UserRepository extends JpaRepository<UserModel, UUID> {
    
}