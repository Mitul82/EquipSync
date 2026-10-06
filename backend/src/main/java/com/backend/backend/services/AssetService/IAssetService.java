package com.backend.backend.services.AssetService;

import java.util.List;
import java.util.UUID;

import com.backend.backend.dto.AssetDTO;
import com.backend.backend.enums.EAssetStatus;
import com.backend.backend.requests.CreateAssetRequest;

public interface IAssetService {
    AssetDTO createAsset(CreateAssetRequest req);

    AssetDTO getUserAsset();

    List<AssetDTO> getAllAssets();

    List<AssetDTO> getAvailableAssets();

    AssetDTO updateAssetStatus(UUID assetId, EAssetStatus status);
}