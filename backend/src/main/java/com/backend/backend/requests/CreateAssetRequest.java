package com.backend.backend.requests;

import com.backend.backend.enums.EAssetStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateAssetRequest(
    @NotBlank(message="Asset name is required") String name, 
    @NotBlank(message="Asset serial number is required") String serialNumber, 
    @NotNull(message="Asset status is required") EAssetStatus status
) {

}