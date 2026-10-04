package com.backend.backend.controllers;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.backend.backend.dto.UserDTO;
import com.backend.backend.requests.LoginRequest;
import com.backend.backend.requests.SignupRequest;
import com.backend.backend.security.JwtService;
import com.backend.backend.security.RateLimitService;
import com.backend.backend.services.AuthService.IAuthService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(
    controllers = AuthControllers.class, 
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
public class AuthControllersTest {
    @org.springframework.boot.test.context.TestConfiguration
    static class JacksonTestConfig {
        @org.springframework.context.annotation.Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    @Autowired private ObjectMapper objectMapper;
    @Autowired private WebApplicationContext context;

    @MockitoBean private JwtService jwtService;
    @MockitoBean private IAuthService authService;
    @MockitoBean private RateLimitService rateLimitService;
    @MockitoBean private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    private MockMvc mockMvc;
    private UserDTO mockUserDTO;
    private final String MOCK_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.mock_payload.mock_signature";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();

        mockUserDTO = new UserDTO();
        mockUserDTO.setId(UUID.randomUUID());
        mockUserDTO.setEmail("mitul@company.com");
        mockUserDTO.setRole("Employee");

        when(rateLimitService.tryConsume(anyString())).thenReturn(true);
    }

    @Test
    public void getCSRFToken_Returns204NoContent() throws Exception {
        mockMvc.perform(get("/api/v1/auth/csrf"))
               .andExpect(status().isNoContent());
    }

    @Test
    public void userLogin_WithValidCredentials_Returns200AndSetsCookie() throws Exception {
        LoginRequest request = new LoginRequest("mitul@company.com", "Password123!");

        when(authService.login(request.email(), request.password())).thenReturn(mockUserDTO);
        when(jwtService.generateToken(anyMap(), eq(request.email()))).thenReturn(MOCK_TOKEN);

        mockMvc.perform(post("/api/v1/auth/login")
               .with(csrf())
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.message").value("Login successfull"))
               .andExpect(jsonPath("$.data.email").value(request.email()))
               .andExpect(cookie().value("ACCESS_TOKEN", MOCK_TOKEN))
               .andExpect(cookie().httpOnly("ACCESS_TOKEN", true))
               .andExpect(cookie().path("ACCESS_TOKEN", "/"));
    }

    @Test
    public void userLogin_WithInvalidCredentials_Returns401Unauthorized() throws Exception {
        LoginRequest request = new LoginRequest("mitul@company.com", "WrongPassword!");

        when(authService.login(request.email(), request.password()))
            .thenThrow(new BadCredentialsException("Invalid Email or Password"));

        mockMvc.perform(post("/api/v1/auth/login")
               .with(csrf())
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isUnauthorized())
               .andExpect(jsonPath("$.message").value("Invalid Email or Password"))
               .andExpect(cookie().doesNotExist("ACCESS_TOKEN"));
    }

    @Test
    public void userSignup_WithValidPayload_Returns200AndSetsCookie() throws Exception {
        SignupRequest request = new SignupRequest("mitul@company.com", "Password123!", "Engineering");

        when(authService.signup(request.email(), request.password(), request.department())).thenReturn(mockUserDTO);
        when(jwtService.generateToken(anyMap(), eq(request.email()))).thenReturn(MOCK_TOKEN);

        mockMvc.perform(post("/api/v1/auth/signup")
               .with(csrf())
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.message").value("Sigenup successfull"))
               .andExpect(cookie().value("ACCESS_TOKEN", MOCK_TOKEN))
               .andExpect(cookie().httpOnly("ACCESS_TOKEN", true));
    }

    @Test
    public void userSignup_WhenDefaultRoleMissing_Returns500ServerError() throws Exception {
        SignupRequest request = new SignupRequest("mitul@company.com", "Password123!", "Engineering");

        when(authService.signup(request.email(), request.password(), request.department()))
            .thenThrow(new IllegalStateException("Default role is missing"));

        mockMvc.perform(post("/api/v1/auth/signup")
               .with(csrf())
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isInternalServerError())
               .andExpect(jsonPath("$.message").value("Default role is missing"));
    }

    @Test
    public void logout_Returns204AndClearsCookie() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
               .with(csrf()))
               .andExpect(status().isNoContent())
               .andExpect(cookie().value("ACCESS_TOKEN", ""))
               .andExpect(cookie().maxAge("ACCESS_TOKEN", 0))
               .andDo(print());
    }
}