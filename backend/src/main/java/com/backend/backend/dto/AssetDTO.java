package com.backend.backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.backend.backend.enums.EAssetStatus;

import lombok.Data;

@Data
public class AssetDTO {
    private UUID id;

    private String name;

    private String serialNumber;

    private EAssetStatus status;

    private UserDTO assignedTo;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}