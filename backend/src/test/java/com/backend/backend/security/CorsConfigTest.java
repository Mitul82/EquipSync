package com.backend.backend.security;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

public class CorsConfigTest {

    private CorsConfig corsConfig;
    private final String MOCK_FRONTEND_URL = "http://localhost:5173";

    @BeforeEach
    void setUp() {
        corsConfig = new CorsConfig();

        ReflectionTestUtils.setField(corsConfig, "fronted_url", MOCK_FRONTEND_URL);
    }

    @Test
    public void corsConfigurationSource_ConfiguresCorsCorrectlyForApiRoutes() {
        CorsConfigurationSource source = corsConfig.corsConfigurationSource();

        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setRequestURI("/api/v1/users/get-current");
        
        CorsConfiguration config = source.getCorsConfiguration(mockRequest);

        assertNotNull(config, "Configuration should be applied to API routes");

        assertTrue(config.getAllowCredentials(), "Allow credentials must be true for JWT cookies");

        assertEquals(1, config.getAllowedOrigins().size());
        assertTrue(config.getAllowedOrigins().contains(MOCK_FRONTEND_URL));

        List<String> expectedMethods = List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
        assertTrue(config.getAllowedMethods().containsAll(expectedMethods));

        List<String> expectedHeaders = List.of("Content-Type", "X-XSRF-TOKEN");
        assertTrue(config.getAllowedHeaders().containsAll(expectedHeaders));
    }
}