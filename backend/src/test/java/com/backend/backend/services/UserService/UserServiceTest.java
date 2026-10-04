package com.backend.backend.services.UserService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.backend.backend.dto.UserDTO;
import com.backend.backend.enums.ERoles;
import com.backend.backend.exceptions.ResourceNotFound;
import com.backend.backend.models.RoleModel;
import com.backend.backend.models.UserModel;
import com.backend.backend.repository.RolesRepository;
import com.backend.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock private UserRepository repo;
    @Mock private RolesRepository roleRepo;
    @Mock private ModelMapper mapper;

    @InjectMocks
    private UserService userService;

    private UserModel mockUser;
    private RoleModel mockRole;
    private UserDTO mockUserDTO;

    private final String TEST_EMAIL = "mitul@company.com";
    private final UUID USER_ID = UUID.randomUUID();

    @BeforeEach
    void setup() {
        mockRole = new RoleModel();
        mockRole.setId(UUID.randomUUID());
        mockRole.setRole(ERoles.Manager);

        mockUser = new UserModel();
        mockUser.setId(USER_ID);
        mockUser.setEmail(TEST_EMAIL);
        mockUser.setRoles(mockRole);

        mockUserDTO = new UserDTO();
        mockUserDTO.setId(mockUser.getId());
        mockUserDTO.setEmail(mockUser.getEmail());
    }

    @Test
    public void assignRoleToUser_WhenValid_UpdatesRoleAndReturnsDTO() {
        when(repo.findById(USER_ID)).thenReturn(Optional.of(mockUser));
        when(roleRepo.findByRole(ERoles.Manager)).thenReturn(Optional.of(mockRole));
        when(repo.save(any(UserModel.class))).thenReturn(mockUser);
        when(mapper.map(mockUser, UserDTO.class)).thenReturn(mockUserDTO);

        UserDTO result = userService.assignRoleToUser(USER_ID, ERoles.Manager);

        assertNotNull(result);
        assertEquals("Manager", result.getRole());
        assertEquals(mockRole, mockUser.getRoles());

        verify(repo, times(1)).save(mockUser);
    }

    @Test
    public void assignRoleToUser_WhenUserNotFound_ThrowsException() {
        when(repo.findById(USER_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFound.class, () -> {
            userService.assignRoleToUser(USER_ID, ERoles.Manager);
        });

        verify(roleRepo, never()).findByRole(any());
        verify(repo, never()).save(any());
    }

    @Test
    public void assignRoleToUser_WhenRoleNotFound_ThrowsException() {
        when(repo.findById(USER_ID)).thenReturn(Optional.of(mockUser));
        when(roleRepo.findByRole(ERoles.Admin)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFound.class, () -> {
            userService.assignRoleToUser(USER_ID, ERoles.Admin);
        });

        verify(repo, never()).save(any());
    }

    @Test
    void getAllUsers_ReturnsMappedList() {
        when(repo.findAll()).thenReturn(List.of(mockUser));
        when(mapper.map(mockUser, UserDTO.class)).thenReturn(mockUserDTO);

        List<UserDTO> result = userService.getAllUsers();

        assertEquals(1, result.size());
        assertEquals(USER_ID, result.get(0).getId());
        verify(repo, times(1)).findAll();
    }

    @Test
    void getCurrentUser_WhenValid_ReturnsDTO() {
        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(auth.getName()).thenReturn(TEST_EMAIL);
        SecurityContextHolder.setContext(securityContext);

        when(repo.findByEmailIgnoreCase(TEST_EMAIL)).thenReturn(Optional.of(mockUser));
        when(mapper.map(mockUser, UserDTO.class)).thenReturn(mockUserDTO);

        UserDTO result = userService.getCurrentUser();

        assertNotNull(result);
        assertEquals("Manager", result.getRole());
        verify(repo, times(1)).findByEmailIgnoreCase(TEST_EMAIL);
    }

    @Test
    void getCurrentUser_WhenUserNotFound_ThrowsException() {
        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(auth.getName()).thenReturn("ghost@company.com");
        SecurityContextHolder.setContext(securityContext);

        when(repo.findByEmailIgnoreCase("ghost@company.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFound.class, () -> {
            userService.getCurrentUser();
        });
    }
}
