package com.backend.backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.backend.backend.enums.ERequestStatus;

import lombok.Data;

@Data
public class EquipmentRequestDTO {
    private UUID id;

    private UserDTO requester;

    private String description;

    private ERequestStatus status;

    private UserDTO reviewedBy;

    private AssetDTO assignedAsset;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}