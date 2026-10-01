package com.backend.backend.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.backend.models.RoleModel;

public interface RolesRepository extends JpaRepository<RoleModel, UUID> {
    
}