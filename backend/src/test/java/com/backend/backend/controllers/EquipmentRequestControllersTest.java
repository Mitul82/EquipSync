package com.backend.backend.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import java.util.UUID;

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

import com.backend.backend.dto.EquipmentRequestDTO;
import com.backend.backend.security.JwtService;
import com.backend.backend.security.RateLimitService;
import com.backend.backend.services.EquipmentRequestService.IEquipmentRequestService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(
    controllers = EquipmentRequestControllers.class,
    properties = {
        "api.prefix=/api/v1",
        "auth.url.frontend_url=http://localhost:5173"
    }
)
@Import({
    com.backend.backend.security.AppUserDetailsService.class,
    com.backend.backend.security.CorsConfig.class,
    com.backend.backend.security.JwtCookieAuthenticationFilter.class,
    com.backend.backend.security.JwtService.class,
    com.backend.backend.security.RateLimitFilter.class,
    com.backend.backend.security.RateLimitService.class,
    com.backend.backend.security.SecurityConfig.class
})
public class EquipmentRequestControllersTest {
    @org.springframework.boot.test.context.TestConfiguration
    static class JacksonTestConfig {
        @org.springframework.context.annotation.Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    @Autowired private WebApplicationContext context;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private IEquipmentRequestService equipmentRequestService;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private RateLimitService rateLimitService;
    @MockitoBean private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    private MockMvc mockMvc;
    private EquipmentRequestDTO mockRequestDTO;
    private final UUID ASSET_ID = UUID.randomUUID();
    private final UUID REQUEST_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();

        mockRequestDTO = new EquipmentRequestDTO();
        mockRequestDTO.setId(REQUEST_ID);
        mockRequestDTO.setDescription("Need a new monitor");

        when(rateLimitService.tryConsume(anyString())).thenReturn(true);
    }

    @Test
    @WithMockUser(authorities = "Manager")
    public void getPendingRequests_AsManager_Returns200() throws Exception {
        when(equipmentRequestService.getAllPendingRequests()).thenReturn(List.of(mockRequestDTO));

        mockMvc.perform(get("/api/v1/equipment/request/get-pending"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.message").value("Retreived all pending requests"))
               .andExpect(jsonPath("$.data[0].id").value(REQUEST_ID.toString()));
    }

    @Test
    @WithMockUser(authorities = "Employee")
    public void getPendingRequests_AsEmployee_Returns403Forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/equipment/request/get-pending"))
               .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "Employee")
    public void getMyRequests_AsEmployee_Returns200() throws Exception {
        when(equipmentRequestService.getMyRequests()).thenReturn(List.of(mockRequestDTO));

        mockMvc.perform(get("/api/v1/equipment/request/get-my-requests"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.message").value("Retreived all user requests"));
    }

    @Test
    @WithMockUser(authorities = "Employee")
    public void createEquipmentRequest_WithValidString_Returns200() throws Exception {
        String description = "Need a new monitor";
        when(equipmentRequestService.createRequest(anyString())).thenReturn(mockRequestDTO);

        mockMvc.perform(post("/api/v1/equipment/request/create")
               .with(csrf())
               .contentType(MediaType.TEXT_PLAIN)
               .content(description))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.message").value("Created new request successfully"));
    }

    @Test
    @WithMockUser(authorities = "Admin")
    public void denyEquipmentRequest_AsAdmin_Returns200() throws Exception {
        when(equipmentRequestService.denyRequest(REQUEST_ID)).thenReturn(mockRequestDTO);

        mockMvc.perform(patch("/api/v1/equipment/request/deny/{requestId}", REQUEST_ID)
               .with(csrf()))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.message").value("Denied request: " + REQUEST_ID));
    }

    @Test
    @WithMockUser(authorities = "Employee")
    public void denyEquipmentRequest_AsEmployee_Returns403Forbidden() throws Exception {
        mockMvc.perform(patch("/api/v1/equipment/request/deny/{requestId}", REQUEST_ID)
               .with(csrf()))
               .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "Manager")
    public void updateRequestStatus_AsManager_Returns200() throws Exception {
        Map<String, Object> updatePayload = Map.of(
            "assetId", ASSET_ID.toString(),
            "status", "Approved"
        );

        when(equipmentRequestService.updateRequestStatus(any(UUID.class), eq(REQUEST_ID), any())).thenReturn(mockRequestDTO);

        mockMvc.perform(patch("/api/v1/equipment/request/approve/{requestId}", REQUEST_ID)
               .with(csrf())
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(updatePayload)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.message").value("Updated request status successfully"));
    }
}