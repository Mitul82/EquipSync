package com.backend.backend.services.EquipmentRequestService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

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

@ExtendWith(MockitoExtension.class)
public class EquipmentServiceTest {
    @Mock private UserRepository userRepo;
    @Mock private AssetRepository assetRepo;
    @Mock private EquipmentRequestRepository repo;
    @Mock private ModelMapper mapper;

    @InjectMocks
    private EquipmentService equipmentService;

    private UserModel mockUser;
    private EquipmentRequestModel mockRequest;
    private EquipmentRequestDTO mockRequestDTO;
    private AssetModel mockAsset;

    private final String TEST_EMAIL = "employee@company.com";

    @BeforeEach
    void setup() {
        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        lenient().when(securityContext.getAuthentication()).thenReturn(auth);
        lenient().when(auth.getName()).thenReturn(TEST_EMAIL);
        
        SecurityContextHolder.setContext(securityContext);

        mockUser = new UserModel();
        mockUser.setId(UUID.randomUUID());
        mockUser.setEmail(TEST_EMAIL);

        mockRequest = new EquipmentRequestModel();
        mockRequest.setId(UUID.randomUUID());
        mockRequest.setRequester(mockUser);
        mockRequest.setDescription("Need a new monitor");
        mockRequest.setStatus(ERequestStatus.Pending);

        mockRequestDTO = new EquipmentRequestDTO();
        mockRequestDTO.setId(mockRequest.getId());
        mockRequestDTO.setDescription("Need a new monitor");

        mockAsset = new AssetModel();
        mockAsset.setId(UUID.randomUUID());
        mockAsset.setStatus(EAssetStatus.Available);
    }

    @Test
    public void createRequest_WhenUserExists_SavesAndReturnsDTO() {
        when(userRepo.findByEmailIgnoreCase(TEST_EMAIL)).thenReturn(Optional.of(mockUser));
        when(repo.save(any(EquipmentRequestModel.class))).thenReturn(mockRequest);
        when(mapper.map(mockRequest, EquipmentRequestDTO.class)).thenReturn(mockRequestDTO);

        EquipmentRequestDTO result = equipmentService.createRequest("Need a new monitor");

        assertNotNull(result);
        assertEquals("Need a new monitor", result.getDescription());

        verify(repo, times(1)).save(argThat(req -> 
            req.getStatus() == ERequestStatus.Pending && 
            req.getRequester().equals(mockUser)
        ));
    }

    @Test
    public void denyRequest_WhenValid_UpdatesStatusToDenied() {
        when(userRepo.findByEmailIgnoreCase(TEST_EMAIL)).thenReturn(Optional.of(mockUser));
        when(repo.findById(mockRequest.getId())).thenReturn(Optional.of(mockRequest));
        when(repo.save(any(EquipmentRequestModel.class))).thenReturn(mockRequest);
        when(mapper.map(mockRequest, EquipmentRequestDTO.class)).thenReturn(mockRequestDTO);

        equipmentService.denyRequest(mockRequest.getId());

        assertEquals(ERequestStatus.Denied, mockRequest.getStatus());
        assertEquals(mockUser, mockRequest.getReviewedBy());
        verify(repo, times(1)).save(mockRequest);
    }

    @Test
    public void getAllPendingRequests_ReturnsMappedList() {
        when(repo.findAllByStatus(ERequestStatus.Pending)).thenReturn(List.of(mockRequest));
        when(mapper.map(mockRequest, EquipmentRequestDTO.class)).thenReturn(mockRequestDTO);

        List<EquipmentRequestDTO> result = equipmentService.getAllPendingRequests();

        assertEquals(1, result.size());
        verify(repo, times(1)).findAllByStatus(ERequestStatus.Pending);
    }

    @Test
    public void getMyRequests_ReturnsMappedListForLoggedInUser() {
        when(repo.findAllByRequesterEmail(TEST_EMAIL)).thenReturn(List.of(mockRequest));
        when(mapper.map(mockRequest, EquipmentRequestDTO.class)).thenReturn(mockRequestDTO);

        List<EquipmentRequestDTO> result = equipmentService.getMyRequests();

        assertEquals(1, result.size());
        verify(repo, times(1)).findAllByRequesterEmail(TEST_EMAIL);
    }

    @Test
    public void updateRequestStatus_WhenApproved_AssignsAssetAndUpdatesStatus() {
        when(userRepo.findByEmailIgnoreCase(TEST_EMAIL)).thenReturn(Optional.of(mockUser));
        when(repo.findById(mockRequest.getId())).thenReturn(Optional.of(mockRequest));
        when(assetRepo.findById(mockAsset.getId())).thenReturn(Optional.of(mockAsset));
        
        when(repo.save(any(EquipmentRequestModel.class))).thenReturn(mockRequest);
        when(mapper.map(mockRequest, EquipmentRequestDTO.class)).thenReturn(mockRequestDTO);

        equipmentService.updateRequestStatus(mockAsset.getId(), mockRequest.getId(), ERequestStatus.Approved);

        assertEquals(EAssetStatus.Assigned, mockAsset.getStatus());
        assertEquals(mockUser, mockAsset.getAssignedTo());
        verify(assetRepo, times(1)).save(mockAsset);

        assertEquals(ERequestStatus.Approved, mockRequest.getStatus());
        assertEquals(mockUser, mockRequest.getReviewedBy());
        assertEquals(mockAsset, mockRequest.getAssignedAsset());
        verify(repo, times(1)).save(mockRequest);
    }

    @Test
    public void updateRequestStatus_WhenApprovedButAssetIsAlreadyAssigned_ThrowsException() {
        mockAsset.setStatus(EAssetStatus.Assigned);
        
        when(userRepo.findByEmailIgnoreCase(TEST_EMAIL)).thenReturn(Optional.of(mockUser));
        when(repo.findById(mockRequest.getId())).thenReturn(Optional.of(mockRequest));
        when(assetRepo.findById(mockAsset.getId())).thenReturn(Optional.of(mockAsset));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            equipmentService.updateRequestStatus(mockAsset.getId(), mockRequest.getId(), ERequestStatus.Approved);
        });

        assertTrue(exception.getMessage().contains("Asset is not available for assignment"));
        
        verify(assetRepo, never()).save(any());
        verify(repo, never()).save(any());
    }

    @Test
    public void updateRequestStatus_WhenUserNotFound_ThrowsResourceNotFound() {
        when(userRepo.findByEmailIgnoreCase(TEST_EMAIL)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFound.class, () -> {
            equipmentService.updateRequestStatus(UUID.randomUUID(), mockRequest.getId(), ERequestStatus.Approved);
        });

        verify(repo, never()).findById(any());
    }

    @Test
    void updateRequestStatus_WhenNotApproved_DoesNotTouchAsset() {
        UUID requestId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();

        UserModel reviewer = new UserModel();
        EquipmentRequestModel request = new EquipmentRequestModel();
        EquipmentRequestDTO dto = new EquipmentRequestDTO();

        when(userRepo.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(reviewer));
        when(repo.findById(requestId)).thenReturn(Optional.of(request));
        when(repo.save(request)).thenReturn(request);
        when(mapper.map(request, EquipmentRequestDTO.class)).thenReturn(dto);

        EquipmentRequestDTO result = equipmentService.updateRequestStatus(assetId, requestId, ERequestStatus.Denied);

        assertSame(dto, result);
        assertEquals(ERequestStatus.Denied, request.getStatus());
        assertSame(reviewer, request.getReviewedBy());
        assertNull(request.getAssignedAsset());
        verifyNoInteractions(assetRepo);
    }

    @Test
    public void createRequest_WhenUserNotFound_ThrowsResourceNotFound() {
        when(userRepo.findByEmailIgnoreCase(TEST_EMAIL)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFound.class, () -> equipmentService.createRequest("Need a new monitor"));

        verify(repo, never()).save(any());
    }

    @Test
    public void denyRequest_WhenUserNotFound_ThrowsResourceNotFound() {
        when(userRepo.findByEmailIgnoreCase(TEST_EMAIL)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFound.class, () -> equipmentService.denyRequest(mockRequest.getId()));

        verify(repo, never()).findById(any());
    }

    @Test
    public void denyRequest_WhenRequestNotFound_ThrowsResourceNotFound() {
        when(userRepo.findByEmailIgnoreCase(TEST_EMAIL)).thenReturn(Optional.of(mockUser));
        when(repo.findById(mockRequest.getId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFound.class, () -> equipmentService.denyRequest(mockRequest.getId()));

        verify(repo, never()).save(any());
    }

    @Test
    public void updateRequestStatus_WhenRequestNotFound_ThrowsResourceNotFound() {
        when(userRepo.findByEmailIgnoreCase(TEST_EMAIL)).thenReturn(Optional.of(mockUser));
        when(repo.findById(mockRequest.getId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFound.class, () ->
            equipmentService.updateRequestStatus(mockAsset.getId(), mockRequest.getId(), ERequestStatus.Approved));

        verifyNoInteractions(assetRepo);
    }

    @Test
    public void updateRequestStatus_WhenApprovedAndAssetNotFound_ThrowsResourceNotFound() {
        when(userRepo.findByEmailIgnoreCase(TEST_EMAIL)).thenReturn(Optional.of(mockUser));
        when(repo.findById(mockRequest.getId())).thenReturn(Optional.of(mockRequest));
        when(assetRepo.findById(mockAsset.getId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFound.class, () ->
            equipmentService.updateRequestStatus(mockAsset.getId(), mockRequest.getId(), ERequestStatus.Approved));

        verify(assetRepo, never()).save(any());
        verify(repo, never()).save(any());
    }
}
