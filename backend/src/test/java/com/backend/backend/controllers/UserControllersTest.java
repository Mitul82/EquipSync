package com.backend.backend.controllers;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.backend.backend.dto.UserDTO;
import com.backend.backend.enums.ERoles;
import com.backend.backend.security.JwtService;
import com.backend.backend.security.RateLimitService;
import com.backend.backend.services.UserService.IUserService;

@WebMvcTest(
    controllers = UserControllers.class,
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
public class UserControllersTest {
    @Autowired private WebApplicationContext context;

    @MockitoBean private IUserService userService;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private RateLimitService rateLimitService;
    @MockitoBean private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    private MockMvc mockMvc;
    private UserDTO mockUserDTO;
    private final UUID USER_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();

        mockUserDTO = new UserDTO();
        mockUserDTO.setId(USER_ID);
        mockUserDTO.setEmail("employee@company.com");
        mockUserDTO.setRole("Employee");

        when(rateLimitService.tryConsume(anyString())).thenReturn(true);
    }

    @Test
    @WithMockUser(authorities = "Employee")
    public void getCurrentUser_Returns200AndData() throws Exception {
        when(userService.getCurrentUser()).thenReturn(mockUserDTO);

        mockMvc.perform(get("/api/v1/user/get-current"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.message").value(""))
               .andExpect(jsonPath("$.data.email").value("employee@company.com"));
    }

    @Test
    @WithMockUser(authorities = "Admin")
    public void getAllUsers_AsAdmin_Returns200() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(mockUserDTO));

        mockMvc.perform(get("/api/v1/user/get-all"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.message").value("Retreived all users"))
               .andExpect(jsonPath("$.data[0].id").value(USER_ID.toString()));
    }

    @Test
    @WithMockUser(authorities = "Manager")
    public void getAllUsers_AsManager_Returns403Forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/user/get-all"))
               .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "Admin")
    public void updateUserRole_AsAdmin_Returns200() throws Exception {
        UserDTO updatedDTO = new UserDTO();
        updatedDTO.setId(USER_ID);
        updatedDTO.setRole("Manager");

        when(userService.assignRoleToUser(eq(USER_ID), eq(ERoles.Manager))).thenReturn(updatedDTO);

        mockMvc.perform(patch("/api/v1/user/update/{userId}", USER_ID)
               .param("role", "Manager")
               .with(csrf()))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.message").value("Updated user role successfully"))
               .andExpect(jsonPath("$.data.role").value("Manager"));
    }

    @Test
    @WithMockUser(authorities = "Employee")
    public void updateUserRole_AsEmployee_Returns403Forbidden() throws Exception {
        mockMvc.perform(patch("/api/v1/user/update/{userId}", USER_ID)
               .param("role", "Manager")
               .with(csrf()))
               .andExpect(status().isForbidden());
    }
}