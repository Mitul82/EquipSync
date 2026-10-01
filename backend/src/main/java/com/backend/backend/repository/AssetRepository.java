package com.backend.backend.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.backend.models.AssetModel;

public interface AssetRepository extends JpaRepository<AssetModel, UUID> {
    
}