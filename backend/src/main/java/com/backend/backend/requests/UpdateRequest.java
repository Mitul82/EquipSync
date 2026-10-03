package com.backend.backend.requests;

import java.util.UUID;

import com.backend.backend.enums.ERequestStatus;

import jakarta.validation.constraints.NotNull;

public record UpdateRequest(
    @NotNull(message="Status is required") ERequestStatus status,
    UUID assetId
) {

}