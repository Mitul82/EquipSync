package com.backend.backend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.backend.models.EquipmentRequestModel;

public interface EquipmentRequestRepository extends JpaRepository<EquipmentRequestModel, UUID> {
    List<EquipmentRequestModel> findAllByRequesterEmail(String email);
}