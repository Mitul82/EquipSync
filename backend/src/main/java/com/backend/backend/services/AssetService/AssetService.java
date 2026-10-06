package com.backend.backend.services.AssetService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.backend.backend.dto.AssetDTO;
import com.backend.backend.enums.EAssetStatus;
import com.backend.backend.exceptions.ResourceNotFound;
import com.backend.backend.models.AssetModel;
import com.backend.backend.models.UserModel;
import com.backend.backend.repository.AssetRepository;
import com.backend.backend.repository.UserRepository;
import com.backend.backend.requests.CreateAssetRequest;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AssetService implements IAssetService {
    private final AssetRepository repo;
    private final UserRepository userRepo;
    private final ModelMapper mapper;

    @Override
    public AssetDTO getUserAsset() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        UserModel user = userRepo.findByEmailIgnoreCase(email).orElseThrow(() -> new ResourceNotFound("Could not find logged in user"));

        AssetModel assignedAsset = repo.findByAssignedTo(user).orElseThrow(() -> new ResourceNotFound("Could not find any assigned assets"));

        return mapper.map(assignedAsset, AssetDTO.class);
    }

    @Override
    public AssetDTO createAsset(CreateAssetRequest req) {
        if(repo.existsBySerialNumber(req.serialNumber())) {
            throw new IllegalArgumentException("An asset with serial number " + req.serialNumber() + " already exists");
        }

        AssetModel asset = AssetModel.builder()
            .name(req.name())
            .status(req.status())
            .serialNumber(req.serialNumber())
            .build();

        AssetModel savedAsset = repo.save(asset);

        return mapper.map(savedAsset, AssetDTO.class);
    }

    @Override
    public List<AssetDTO> getAllAssets() {
        return repo.findAll().stream().map(asset -> mapper.map(asset, AssetDTO.class)).collect(Collectors.toList());
    }

    @Override
    public List<AssetDTO> getAvailableAssets() {
        return repo.findAllByStatus(EAssetStatus.Available).stream().map(asset -> mapper.map(asset, AssetDTO.class)).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AssetDTO updateAssetStatus(UUID assetId, EAssetStatus status) {
        AssetModel asset = repo.findById(assetId).orElseThrow(() -> new ResourceNotFound("Asset not found"));

        asset.setStatus(status);

        if(status == EAssetStatus.Available || status == EAssetStatus.Retired) {
            asset.setAssignedTo(null);
        }

        AssetModel updatedAsset = repo.save(asset);

        return mapper.map(updatedAsset, AssetDTO.class);
    }
}