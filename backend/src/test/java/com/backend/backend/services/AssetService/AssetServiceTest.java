package com.backend.backend.services.AssetService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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

import com.backend.backend.dto.AssetDTO;
import com.backend.backend.enums.EAssetStatus;
import com.backend.backend.exceptions.ResourceNotFound;
import com.backend.backend.models.AssetModel;
import com.backend.backend.models.UserModel;
import com.backend.backend.repository.AssetRepository;
import com.backend.backend.requests.CreateAssetRequest;

@ExtendWith(MockitoExtension.class)
public class AssetServiceTest {
    @Mock private AssetRepository repo;
    @Mock private ModelMapper mapper;

    @InjectMocks
    private AssetService assetService;

    private AssetDTO mockAssetDTO;
    private AssetModel mockSavedAsset;
    private CreateAssetRequest request;

    @BeforeEach
    void setup() {
        request = new CreateAssetRequest("MacBook Pro", "SN-9999", EAssetStatus.Available);
        
        mockSavedAsset = AssetModel.builder()
                .id(UUID.randomUUID())
                .name(request.name())
                .serialNumber(request.serialNumber())
                .status(request.status())
                .build();

        mockAssetDTO = new AssetDTO();
        mockAssetDTO.setId(mockSavedAsset.getId());
        mockAssetDTO.setName(mockSavedAsset.getName());
    }

    @Test
    public void createAsset_WhenValidRequest_SavesAndReturnsDTO() {
        when(repo.existsBySerialNumber(request.serialNumber())).thenReturn(false);
        when(repo.save(any(AssetModel.class))).thenReturn(mockSavedAsset);
        when(mapper.map(mockSavedAsset, AssetDTO.class)).thenReturn(mockAssetDTO);

        AssetDTO result = assetService.createAsset(request);

        assertNotNull(result);
        assertEquals(mockAssetDTO.getId(), result.getId());

        verify(repo, times(1)).save(any(AssetModel.class));
    }

    @Test
    public void createAsset_WhenSerialNumberExists_ThrowsException() {
        when(repo.existsBySerialNumber(request.serialNumber())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            assetService.createAsset(request);
        });

        assertTrue(exception.getMessage().contains("already exists"));

        verify(repo, never()).save(any(AssetModel.class));
    }

    @Test
    public void getAllAssets_ReturnsMappedListOfAssetDTOs() {
        when(repo.findAll()).thenReturn(List.of(mockSavedAsset));
        when(mapper.map(mockSavedAsset, AssetDTO.class)).thenReturn(mockAssetDTO);

        List<AssetDTO> result = assetService.getAllAssets();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(mockAssetDTO.getId(), result.get(0).getId());

        verify(repo, times(1)).findAll();
    }

    @Test
    public void getAvailableAssets_ReturnsMappedListOfAvailableAssetDTOs() {
        when(repo.findAllByStatus(EAssetStatus.Available)).thenReturn(List.of(mockSavedAsset));
        when(mapper.map(mockSavedAsset, AssetDTO.class)).thenReturn(mockAssetDTO);

        List<AssetDTO> result = assetService.getAvailableAssets();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(mockAssetDTO.getId(), result.get(0).getId());

        verify(repo, times(1)).findAllByStatus(EAssetStatus.Available);
    }

    @Test
    public void updateAssetStatus_WhenAssetNotFound_ThrowsException() {
        UUID fakeId = UUID.randomUUID();
        when(repo.findById(fakeId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFound.class, () -> {
            assetService.updateAssetStatus(fakeId, EAssetStatus.Available);
        });

        verify(repo, never()).save(any());
    }

    @Test
    public void updateAssetStatus_WhenStatusIsNotAvailableOrRetired_KeepsAssignedUser() {
        UserModel employee = new UserModel();
        mockSavedAsset.setAssignedTo(employee);

        when(repo.findById(mockSavedAsset.getId())).thenReturn(Optional.of(mockSavedAsset));
        when(repo.save(any(AssetModel.class))).thenReturn(mockSavedAsset);
        when(mapper.map(mockSavedAsset, AssetDTO.class)).thenReturn(mockAssetDTO);

        AssetDTO result = assetService.updateAssetStatus(mockSavedAsset.getId(), EAssetStatus.Assigned);

        assertNotNull(result);
        assertNotNull(mockSavedAsset.getAssignedTo(), "Assigned user should NOT be cleared");
        assertEquals(employee, mockSavedAsset.getAssignedTo());

        verify(repo, times(1)).save(mockSavedAsset);
    }

    @Test
    public void updateAssetStatus_WhenStatusIsAvailable_ClearsAssignedUser() {
        UserModel employee = new UserModel();
        mockSavedAsset.setAssignedTo(employee);
        
        when(repo.findById(mockSavedAsset.getId())).thenReturn(Optional.of(mockSavedAsset));
        when(repo.save(any(AssetModel.class))).thenReturn(mockSavedAsset);
        when(mapper.map(mockSavedAsset, AssetDTO.class)).thenReturn(mockAssetDTO);

        AssetDTO result = assetService.updateAssetStatus(mockSavedAsset.getId(), EAssetStatus.Available);

        assertNotNull(result);
        assertNull(mockSavedAsset.getAssignedTo(), "Assigned user should be cleared when Available");
        assertEquals(EAssetStatus.Available, mockSavedAsset.getStatus());

        verify(repo, times(1)).save(mockSavedAsset);
    }

    @Test
    public void updateAssetStatus_WhenStatusIsRetired_ClearsAssignedUser() {
        mockSavedAsset.setAssignedTo(new UserModel());
    
        when(repo.findById(mockSavedAsset.getId())).thenReturn(Optional.of(mockSavedAsset));
        when(repo.save(any(AssetModel.class))).thenReturn(mockSavedAsset);
        when(mapper.map(mockSavedAsset, AssetDTO.class)).thenReturn(mockAssetDTO);
    
        assetService.updateAssetStatus(mockSavedAsset.getId(), EAssetStatus.Retired);
    
        assertNull(mockSavedAsset.getAssignedTo(), "Assigned user should be cleared when Retired");
        assertEquals(EAssetStatus.Retired, mockSavedAsset.getStatus());
    }
}