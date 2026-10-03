package com.backend.backend.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.backend.dto.EquipmentRequestDTO;
import com.backend.backend.requests.UpdateRequest;
import com.backend.backend.responses.ApiResponse;
import com.backend.backend.services.EquipmentRequestService.IEquipmentRequestService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/equipment/request")
public class EquipmentRequestControllers {
    private final IEquipmentRequestService equipmentRequestService;

    @GetMapping("/get-pending")
    @PreAuthorize("hasAnyAuthority('Admin', 'Manager')")
    public ResponseEntity<ApiResponse> getPeningRequests() {
        List<EquipmentRequestDTO> requests = equipmentRequestService.getAllPendingRequests();

        return ResponseEntity.ok().body(new ApiResponse("Retreived all pending requests", requests));
    }
    
    @GetMapping("/get-my-requests")
    public ResponseEntity<ApiResponse> getMyrequests() {
        List<EquipmentRequestDTO> requests = equipmentRequestService.getMyRequests();

        return ResponseEntity.ok().body(new ApiResponse("Retreived all user requests", requests));
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createEquipmentRequest(@RequestBody String description) {
        EquipmentRequestDTO requestDTO = equipmentRequestService.createRequest(description);

        return ResponseEntity.ok().body(new ApiResponse("Created new request successfully", requestDTO));
    }

    @PatchMapping("/deny/{requestId}")
    @PreAuthorize("hasAnyAuthority('Admin', 'Manager')")
    public ResponseEntity<ApiResponse> denyEquipmentRequest(@PathVariable UUID requestId) {
        EquipmentRequestDTO requestDTO = equipmentRequestService.denyRequest(requestId);

        return ResponseEntity.ok().body(new ApiResponse("Denied request: " + requestId, requestDTO));
    }

    @PatchMapping("/approve/{requestId}")
    @PreAuthorize("hasAnyAuthority('Admin', 'Manager')")
    public ResponseEntity<ApiResponse> updateRequestStatus(@PathVariable UUID requestId, @Valid @RequestBody UpdateRequest req) {
        EquipmentRequestDTO request = equipmentRequestService.updateRequestStatus(req.assetId(), requestId, req.status());

        return ResponseEntity.ok().body(new ApiResponse("Updated request status successfully", request));
    }
}