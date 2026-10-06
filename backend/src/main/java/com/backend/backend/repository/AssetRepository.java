package com.backend.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.backend.enums.EAssetStatus;
import com.backend.backend.models.AssetModel;
import com.backend.backend.models.UserModel;

public interface AssetRepository extends JpaRepository<AssetModel, UUID> {
    List<AssetModel> findAllByStatus(EAssetStatus status);

    boolean existsBySerialNumber(String serialNumber);

    Optional<AssetModel> findByAssignedTo(UserModel assignedTo);
}