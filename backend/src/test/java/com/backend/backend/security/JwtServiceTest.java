package com.backend.backend.security;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.JwtException;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {
    private JwtService jwtService;
    private UserDetails mockUser;
    
    private final String TEST_EMAIL = "employee@company.com";

    private final String TEST_SECRET = "TXktU3VwZXItU2VjcmV0LUtleS1UaGF0LU11c3QtQmUtQXQtTGVhc3QtMzItQnl0ZXMtTG9uZyE="; 
    private final long TEST_EXPIRATION = 86400000L;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        ReflectionTestUtils.setField(jwtService, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "expirationMs", TEST_EXPIRATION);

        ReflectionTestUtils.invokeMethod(jwtService, "initialize");

        mockUser = new User(
            TEST_EMAIL, 
            "password123", 
            Collections.singletonList(new SimpleGrantedAuthority("Manager"))
        );
    }

    @Test
    public void generateToken_ReturnsValidJwtString() {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "Manager");

        String token = jwtService.generateToken(extraClaims, TEST_EMAIL);

        assertNotNull(token);
        assertEquals(3, token.split("\\.").length); 
    }

    @Test
    public void extractUsername_WhenValidToken_ReturnsSubject() {
        String token = jwtService.generateToken(new HashMap<>(), TEST_EMAIL);

        String extractedUsername = jwtService.extractUsername(token);

        assertEquals(TEST_EMAIL, extractedUsername);
    }

    @Test
    public void isTokenValid_WhenUsernameMatches_ReturnsTrue() {
        String token = jwtService.generateToken(new HashMap<>(), TEST_EMAIL);

        boolean isValid = jwtService.isTokenValid(token, mockUser);

        assertTrue(isValid);
    }

    @Test
    public void isTokenValid_WhenUsernameMismatches_ReturnsFalse() {
        String token = jwtService.generateToken(new HashMap<>(), "someone.else@company.com");

        boolean isValid = jwtService.isTokenValid(token, mockUser);

        assertFalse(isValid);
    }

    @Test
    public void parse_WhenTokenIsTampered_ThrowsJwtException() {
        String tamperedToken = "this.is.a.completely.invalid.token";

        assertThrows(JwtException.class, () -> {
            jwtService.extractUsername(tamperedToken);
        });
    }
}