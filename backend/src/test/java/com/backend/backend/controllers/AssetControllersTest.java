package com.backend.backend.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.backend.backend.dto.AssetDTO;
import com.backend.backend.enums.EAssetStatus;
import com.backend.backend.exceptions.GlobalExceptionHandler;
import com.backend.backend.exceptions.ResourceNotFound;
import com.backend.backend.requests.CreateAssetRequest;
import com.backend.backend.security.JwtService;
import com.backend.backend.security.RateLimitService;
import com.backend.backend.services.AssetService.IAssetService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(
    controllers = AssetControllers.class,
    properties = {
        "api.prefix=/api/v1",
        "auth.url.frontend_url=http://localhost:5173"
    }
)
@Import({
    GlobalExceptionHandler.class,
    com.backend.backend.security.AppUserDetailsService.class,
    com.backend.backend.security.CorsConfig.class,
    com.backend.backend.security.JwtCookieAuthenticationFilter.class,
    com.backend.backend.security.JwtService.class,
    com.backend.backend.security.RateLimitFilter.class,
    com.backend.backend.security.RateLimitService.class,
    com.backend.backend.security.SecurityConfig.class
})
public class AssetControllersTest {
    @org.springframework.boot.test.context.TestConfiguration
    static class JacksonTestConfig {
        @org.springframework.context.annotation.Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    @Autowired private WebApplicationContext context;
    @Autowired private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @MockitoBean private JwtService jwtService;
    @MockitoBean private IAssetService assetService;
    @MockitoBean private RateLimitService rateLimitService;
    @MockitoBean private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    private AssetDTO mockAsset;
    private final UUID ASSET_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        mockAsset = new AssetDTO();
        mockAsset.setId(ASSET_ID);
        mockAsset.setName("Dell XPS 15");

        when(rateLimitService.tryConsume(anyString())).thenReturn(true);
    }

    @Test
    @WithMockUser(authorities = "Admin")
    public void resourceNotFound_Returns404WithMessage() throws Exception {
        when(assetService.updateAssetStatus(any(UUID.class), any(EAssetStatus.class)))
            .thenThrow(new ResourceNotFound("Asset not found"));

        mockMvc.perform(patch("/api/v1/asset/update/{assetId}", ASSET_ID)
               .param("status", "Assigned")
               .with(csrf()))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.message").value("Asset not found"));
    }

    @Test
    @WithMockUser(authorities = "Admin")
    public void illegalArgument_Returns409WithMessage() throws Exception {
        when(assetService.updateAssetStatus(any(UUID.class), any(EAssetStatus.class)))
            .thenThrow(new IllegalArgumentException("Invalid status transition"));

        mockMvc.perform(patch("/api/v1/asset/update/{assetId}", ASSET_ID)
               .param("status", "Assigned")
               .with(csrf()))
               .andExpect(status().isConflict())
               .andExpect(jsonPath("$.message").value("Invalid status transition"));
    }

    @Test
    @WithMockUser(authorities = "Manager")
    public void illegalState_Returns409WithMessage() throws Exception {
        CreateAssetRequest request = new CreateAssetRequest("Dell XPS 15", "SN-123", EAssetStatus.Available);
        when(assetService.createAsset(any(CreateAssetRequest.class)))
            .thenThrow(new IllegalStateException("Serial number already exists"));

        mockMvc.perform(post("/api/v1/asset/create")
               .with(csrf())
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isConflict())
               .andExpect(jsonPath("$.message").value("Serial number already exists"));
    }

    @Test
    @WithMockUser(authorities = "Manager")
    public void invalidBody_Returns400WithFieldMessage() throws Exception {
        CreateAssetRequest invalid = new CreateAssetRequest("", "", EAssetStatus.Available);

        mockMvc.perform(post("/api/v1/asset/create")
               .with(csrf())
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(invalid)))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @WithMockUser(authorities = "Admin")
    public void unexpectedException_Returns500WithGenericMessage() throws Exception {
        when(assetService.getAllAssets()).thenThrow(new RuntimeException("db exploded"));

        mockMvc.perform(get("/api/v1/asset/get-all"))
               .andExpect(status().isInternalServerError())
               .andExpect(jsonPath("$.message").value("An unexpected internal server error occurred"));
    }

    @Test
    @WithMockUser(authorities = "Employee")
    public void accessDenied_Returns403WithMessage() throws Exception {
        mockMvc.perform(get("/api/v1/asset/get-all"))
               .andExpect(status().isForbidden())
               .andExpect(jsonPath("$.message").value("You do not have permission to perform this action"));
    }

    @Test
    @WithMockUser(authorities = "Employee")
    public void getAssignedAsset_AsEmployee_Returns200AndAsset() throws Exception {
        when(assetService.getUserAsset()).thenReturn(mockAsset);
    
        mockMvc.perform(get("/api/v1/asset/get-assigned"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.message").value("Retreived assigned user asset"))
               .andExpect(jsonPath("$.data.name").value("Dell XPS 15"));
    }
    
    @Test
    @WithMockUser(authorities = "Employee")
    public void getAssignedAsset_WhenNoAssetAssigned_Returns404() throws Exception {
        when(assetService.getUserAsset())
            .thenThrow(new ResourceNotFound("Could not find any assigned assets"));
    
        mockMvc.perform(get("/api/v1/asset/get-assigned"))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.message").value("Could not find any assigned assets"));
    }
    
    @Test
    public void getAssignedAsset_Unauthenticated_Returns403() throws Exception {
        mockMvc.perform(get("/api/v1/asset/get-assigned"))
               .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "Admin")
    public void getAllAssets_AsAdmin_Returns200AndData() throws Exception {
        when(assetService.getAllAssets()).thenReturn(List.of(mockAsset));
    
        mockMvc.perform(get("/api/v1/asset/get-all"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.message").value("Retreived all assets"))
               .andExpect(jsonPath("$.data[0].name").value("Dell XPS 15"));
    }
    
    @Test
    @WithMockUser(authorities = "Employee")
    public void getAvailableAssets_AsEmployee_Returns200() throws Exception {
        when(assetService.getAvailableAssets()).thenReturn(List.of(mockAsset));
    
        mockMvc.perform(get("/api/v1/asset/get-available"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.message").value("Retreived all available assests"))
               .andExpect(jsonPath("$.data[0].name").value("Dell XPS 15"));
    }
    
    @Test
    @WithMockUser(authorities = "Manager")
    public void createAsset_AsManagerWithValidPayload_Returns200() throws Exception {
        CreateAssetRequest request = new CreateAssetRequest("Dell XPS 15", "SN-123", EAssetStatus.Available);
        when(assetService.createAsset(any(CreateAssetRequest.class))).thenReturn(mockAsset);
    
        mockMvc.perform(post("/api/v1/asset/create")
               .with(csrf())
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.message").value("Asset created succesfully"))
               .andExpect(jsonPath("$.data.id").value(ASSET_ID.toString()));
    }
    
    @Test
    @WithMockUser(authorities = "Employee")
    public void createAsset_AsEmployee_Returns403Forbidden() throws Exception {
        CreateAssetRequest request = new CreateAssetRequest("Dell XPS 15", "SN-123", EAssetStatus.Available);
    
        mockMvc.perform(post("/api/v1/asset/create")
               .with(csrf())
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(authorities = "Admin")
    public void updateAsset_AsAdmin_Returns200() throws Exception {
        when(assetService.updateAssetStatus(any(UUID.class), any(EAssetStatus.class))).thenReturn(mockAsset);
    
        mockMvc.perform(patch("/api/v1/asset/update/{assetId}", ASSET_ID)
               .param("status", "Assigned")
               .with(csrf()))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.message").value("Updated asset status"));
    }
}