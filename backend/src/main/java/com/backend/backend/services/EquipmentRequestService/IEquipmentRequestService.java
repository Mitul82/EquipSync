package com.backend.backend.services.EquipmentRequestService;

import java.util.List;
import java.util.UUID;

import com.backend.backend.dto.EquipmentRequestDTO;
import com.backend.backend.enums.ERequestStatus;

public interface IEquipmentRequestService {
    List<EquipmentRequestDTO> getMyRequests();

    List<EquipmentRequestDTO> getAllPendingRequests();

    EquipmentRequestDTO createRequest(String description);
    
    EquipmentRequestDTO denyRequest(UUID requestId);
    
    EquipmentRequestDTO updateRequestStatus(UUID assetId, UUID requestId, ERequestStatus status);
}