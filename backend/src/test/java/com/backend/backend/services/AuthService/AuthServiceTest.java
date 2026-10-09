package com.backend.backend.services.AuthService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.backend.backend.dto.UserDTO;
import com.backend.backend.enums.ERoles;
import com.backend.backend.models.RoleModel;
import com.backend.backend.models.UserModel;
import com.backend.backend.repository.RolesRepository;
import com.backend.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock private UserRepository userRepo;
    @Mock private RolesRepository roleRepo;
    @Mock PasswordEncoder encoder;
    @Mock ModelMapper mapper;

    @InjectMocks
    private AuthService authService;

    private UserModel mockUser;
    private RoleModel mockRole;
    private UserDTO mockUserDTO;

    private final String TEST_EMAIL = "test@company.com";
    private final String RAW_PASSWORD = "Password123!";
    private final String HASHED_PASSWORD = "hashed_password_abc";

    @BeforeEach
    void setup() {
        mockRole = new RoleModel();
        mockRole.setId(UUID.randomUUID());
        mockRole.setRole(ERoles.Employee);

        mockUser = new UserModel();
        mockUser.setId(UUID.randomUUID());
        mockUser.setEmail(TEST_EMAIL);
        mockUser.setPassword(HASHED_PASSWORD);
        mockUser.setDepartment("Engineering");
        mockUser.setRole(mockRole);

        mockUserDTO = new UserDTO();
        mockUserDTO.setId(mockUser.getId());
        mockUserDTO.setEmail(mockUser.getEmail());
    }

    @Test
    public void login_WhenCredentialsAreValid_ReturnsUserDTO() {
        when(userRepo.findByEmailIgnoreCase(TEST_EMAIL)).thenReturn(Optional.of(mockUser));
        when(encoder.matches(RAW_PASSWORD, HASHED_PASSWORD)).thenReturn(true);
        when(mapper.map(mockUser, UserDTO.class)).thenReturn(mockUserDTO);

        UserDTO result = authService.login(TEST_EMAIL, RAW_PASSWORD);

        assertNotNull(result);
        assertEquals(TEST_EMAIL, result.getEmail());
        assertEquals("Employee", result.getRole());
    }

    @Test
    public void login_WhenEmailNotFound_ThrowsBadCredtialsException() {
        when(userRepo.findByEmailIgnoreCase("wrong@company.com")).thenReturn(Optional.empty());

        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            authService.login("wrong@company.com", RAW_PASSWORD);
        });

        assertEquals("Invalid Email or password", exception.getMessage());
        verify(encoder, never()).matches(anyString(), anyString());
    }

    @Test
    public void login_WhenPasswordIsIncorrect_ThrowsBadCredentialsException() {
        when(userRepo.findByEmailIgnoreCase(TEST_EMAIL)).thenReturn(Optional.of(mockUser));
        when(encoder.matches("WrongPassword", HASHED_PASSWORD)).thenReturn(false);

        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            authService.login(TEST_EMAIL, "WrongPassword");
        });

        assertEquals("Invalid Email or Password", exception.getMessage());
    }

    @Test
    public void signup_WhenValidRequest_SavesUserAndReturnsDTO() {
        when(roleRepo.findByRole(ERoles.Employee)).thenReturn(Optional.of(mockRole));
        when(encoder.encode(RAW_PASSWORD)).thenReturn(HASHED_PASSWORD);
        when(userRepo.save(any(UserModel.class))).thenReturn(mockUser);
        when(mapper.map(mockUser, UserDTO.class)).thenReturn(mockUserDTO);

        UserDTO result = authService.signup(TEST_EMAIL, RAW_PASSWORD, "Engineering");

        assertNotNull(result);
        assertEquals("Employee", result.getRole());

        verify(userRepo, times(1)).save(any(UserModel.class));
    }

    @Test
    public void signup_WhenDefaultRoleMissing_ThrowsIllegalStateException() {
        when(roleRepo.findByRole(ERoles.Employee)).thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            authService.signup(TEST_EMAIL, RAW_PASSWORD, "Engineering");
        });

        assertEquals("Default role is missing", exception.getMessage());

        verify(userRepo, never()).save(any(UserModel.class));
    }
}