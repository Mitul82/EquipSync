package com.backend.backend.services.EquipmentRequestService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.backend.dto.EquipmentRequestDTO;
import com.backend.backend.enums.EAssetStatus;
import com.backend.backend.enums.ERequestStatus;
import com.backend.backend.exceptions.ResourceNotFound;
import com.backend.backend.models.AssetModel;
import com.backend.backend.models.EquipmentRequestModel;
import com.backend.backend.models.UserModel;
import com.backend.backend.repository.AssetRepository;
import com.backend.backend.repository.EquipmentRequestRepository;
import com.backend.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EquipmentService implements IEquipmentRequestService {
    private final ModelMapper mapper;
    private final UserRepository userRepo;
    private final AssetRepository assetRepo;
    private final EquipmentRequestRepository repo;

    @Override
    public EquipmentRequestDTO createRequest(String description) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        UserModel user = userRepo.findByEmailIgnoreCase(email).orElseThrow(() -> new ResourceNotFound("Could not find logged in user in database"));

        EquipmentRequestModel request = new EquipmentRequestModel();

        request.setRequester(user);
        request.setDescription(description);
        request.setStatus(ERequestStatus.Pending);

        EquipmentRequestModel savedRequest = repo.save(request);

        return mapper.map(savedRequest, EquipmentRequestDTO.class);
    }

    @Override
    @Transactional
    public EquipmentRequestDTO denyRequest(UUID requestId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        UserModel user = userRepo.findByEmailIgnoreCase(email).orElseThrow(() -> new ResourceNotFound("Could not find logged in user"));

        EquipmentRequestModel request = repo.findById(requestId).orElseThrow(() -> new ResourceNotFound("Equipment request " + requestId + " not found"));

        request.setStatus(ERequestStatus.Denied);
        request.setReviewedBy(user);

        EquipmentRequestModel updatedRequest = repo.save(request);

        return mapper.map(updatedRequest, EquipmentRequestDTO.class);
    }

    @Override
    public List<EquipmentRequestDTO> getAllPendingRequests() {
        List<EquipmentRequestModel> req = repo.findAllByStatus(ERequestStatus.Pending);

        return req.stream().map(request -> mapper.map(request, EquipmentRequestDTO.class)).collect(Collectors.toList());
    }

    @Override
    public List<EquipmentRequestDTO> getMyRequests() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        List<EquipmentRequestModel> requests = repo.findAllByRequesterEmail(email);

        return requests.stream().map(req -> mapper.map(req, EquipmentRequestDTO.class)).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EquipmentRequestDTO updateRequestStatus(UUID assetId, UUID requestId, ERequestStatus status) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        UserModel user = userRepo.findByEmailIgnoreCase(email).orElseThrow(() -> new ResourceNotFound("Could not find logged in user in database"));

        EquipmentRequestModel request = repo.findById(requestId).orElseThrow(() -> new ResourceNotFound("Could not find request with id " + requestId));

        if(status == ERequestStatus.Approved) {
            AssetModel assignedAsset = assetRepo.findById(assetId).orElseThrow(() -> new ResourceNotFound("Requested asset not found"));

            if(assignedAsset.getStatus() == EAssetStatus.Assigned) {
                throw new IllegalStateException("Asset is not available for assignment. Current status: " + assignedAsset.getStatus());
            }

            assignedAsset.setStatus(EAssetStatus.Assigned);
            assignedAsset.setAssignedTo(request.getRequester());

            assetRepo.save(assignedAsset);

            request.setAssignedAsset(assignedAsset);
        }

        request.setStatus(status);
        request.setReviewedBy(user);

        EquipmentRequestModel updatedRequest = repo.save(request);

        return mapper.map(updatedRequest, EquipmentRequestDTO.class);
    }
}