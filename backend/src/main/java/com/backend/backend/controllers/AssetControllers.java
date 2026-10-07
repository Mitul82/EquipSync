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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.backend.dto.AssetDTO;
import com.backend.backend.enums.EAssetStatus;
import com.backend.backend.requests.CreateAssetRequest;
import com.backend.backend.responses.ApiResponse;
import com.backend.backend.services.AssetService.IAssetService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/*
    * IMPORTANT SECURITY NOTE: RBAC Annotations
    * -----------------------------------------
    * Do NOT use @PreAuthorize("hasRole('Admin')"). 
    * 
    * By default, Spring Security's hasRole() method automatically prepends 
    * the prefix "ROLE_" to the string you provide (evaluating as "ROLE_Admin"). 
    * Because our ERoles enum uses exact strings ("Admin", "Manager", "Employee") 
    * without the prefix, hasRole() will always fail and return a 403 Forbidden.
    * 
    * Always use hasAuthority() or hasAnyAuthority() instead. These methods 
    * perform an exact 1-to-1 string match against the user's GrantedAuthorities.
    * 
    * Example: @PreAuthorize("hasAnyAuthority('Admin', 'Manager')")
*/

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/asset")
public class AssetControllers {
    private final IAssetService assetService;

    @GetMapping("/get-all")
    // Note: Use hasAuthority instead of hasRole to prevent Spring's automatic "ROLE_" prefixing
    @PreAuthorize("hasAnyAuthority('Admin', 'Manager')")
    public ResponseEntity<ApiResponse> getAllAssets() {
        List<AssetDTO> assets = assetService.getAllAssets();

        return ResponseEntity.ok().body(new ApiResponse("Retreived all assets", assets));
    }

    @GetMapping("/get-available")
    public ResponseEntity<ApiResponse> getAvailableAssets() {
        List<AssetDTO> assets = assetService.getAvailableAssets();

        return ResponseEntity.ok().body(new ApiResponse("Retreived all available assests", assets));
    }

    @GetMapping("/get-assigned")
    public ResponseEntity<ApiResponse> getAssignedAsset() {
        AssetDTO asset = assetService.getUserAsset();

        return ResponseEntity.ok().body(new ApiResponse("Retreived assigned user asset", asset));
    }

    @PostMapping("/create")
    // Note: Use hasAnyAuthority instead of hasAnyRole to prevent Spring's automatic "ROLE_" prefixing
    @PreAuthorize("hasAnyAuthority('Admin', 'Manager')")
    public ResponseEntity<ApiResponse> createAsset(@Valid @RequestBody CreateAssetRequest req) {
        AssetDTO asset = assetService.createAsset(req);

        return ResponseEntity.ok().body(new ApiResponse("Asset created succesfully", asset));
    }

    @PatchMapping("/update/{assetId}")
    @PreAuthorize("hasAnyAuthority('Admin', 'Manager')")
    public ResponseEntity<ApiResponse> updateAsset(@PathVariable UUID assetId, @RequestParam EAssetStatus status) {
        AssetDTO updatedAsset = assetService.updateAssetStatus(assetId, status);

        return ResponseEntity.ok().body(new ApiResponse("Updated asset status", updatedAsset));
    }
}