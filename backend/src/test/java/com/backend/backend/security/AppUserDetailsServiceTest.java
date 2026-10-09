package com.backend.backend.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.backend.backend.enums.ERoles;
import com.backend.backend.models.RoleModel;
import com.backend.backend.models.UserModel;
import com.backend.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class AppUserDetailsServiceTest {
    @Mock private UserRepository userRepository;

    @InjectMocks
    private AppUserDetailsService appUserDetailsService;

    private UserModel mockUser;
    private RoleModel mockRole;
    
    private final String TEST_EMAIL = "admin@company.com";
    private final String TEST_PASSWORD = "hashed_password_123";

    @BeforeEach
    void setUp() {
        mockRole = new RoleModel();
        mockRole.setId(UUID.randomUUID());
        mockRole.setRole(ERoles.Admin);

        mockUser = new UserModel();
        mockUser.setId(UUID.randomUUID());
        mockUser.setEmail(TEST_EMAIL);
        mockUser.setPassword(TEST_PASSWORD);
        mockUser.setRole(mockRole);
    }

    @Test
    public void loadUserByUsername_WhenUserExists_ReturnsUserDetails() {
        when(userRepository.findByEmailIgnoreCase(TEST_EMAIL)).thenReturn(Optional.of(mockUser));

        UserDetails userDetails = appUserDetailsService.loadUserByUsername(TEST_EMAIL);

        assertNotNull(userDetails);
        assertEquals(TEST_EMAIL, userDetails.getUsername());
        assertEquals(TEST_PASSWORD, userDetails.getPassword());
        assertEquals(1, userDetails.getAuthorities().size());

        assertTrue(userDetails.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("Admin")));
            
        verify(userRepository, times(1)).findByEmailIgnoreCase(TEST_EMAIL);
    }

    @Test
    public void loadUserByUsername_WhenUserNotFound_ThrowsException() {
        when(userRepository.findByEmailIgnoreCase("ghost@company.com")).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            appUserDetailsService.loadUserByUsername("ghost@company.com");
        });

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findByEmailIgnoreCase("ghost@company.com");
    }
}