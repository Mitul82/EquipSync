package com.backend.backend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.backend.enums.EAssetStatus;
import com.backend.backend.models.AssetModel;

public interface AssetRepository extends JpaRepository<AssetModel, UUID> {
    List<AssetModel> findAllByStatus(EAssetStatus status);

    boolean existsBySerialNumber(String serialNumber);
}