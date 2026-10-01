package com.backend.backend.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.backend.models.EquipmentRequestModel;

public interface EquipmentRequestRepository extends JpaRepository<EquipmentRequestModel, UUID> {
    
}