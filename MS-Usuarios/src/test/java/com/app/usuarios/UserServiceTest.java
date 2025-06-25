package com.app.usuarios;

import com.app.usuarios.Controller.UserAlreadyExistsException;
import com.app.usuarios.Dto.*;
import com.app.usuarios.Model.Role;
import com.app.usuarios.Model.User;
import com.app.usuarios.Repository.RoleRepository;
import com.app.usuarios.Repository.UserRepository;
import com.app.usuarios.Service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.management.relation.RoleNotFoundException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private Role testRole;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
         testRole= Role.builder()
                .name("ROLE_USER")
                .build();

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .roles(Collections.singleton(testRole))
                .locked(false)
                .enabled(true)
                .build();

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
    }

    @Test
    void getAllUsers_ShouldReturnListOfUsers() {
        
        List<User> users = Collections.singletonList(testUser);
        when(userRepository.findAll()).thenReturn(users);

        
        ServiceResult<List<UserResponseDto>> result = userService.getAllUsers();

        
        assertNotNull(result.getData());
        assertEquals(1, result.getData().size());
        assertEquals("testuser", result.getData().get(0).getUsername());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getAllUsers_ShouldReturnErrorWhenExceptionOccurs() {
        
        when(userRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        
        ServiceResult<List<UserResponseDto>> result = userService.getAllUsers();

        
        assertTrue(result.hasErrors());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().get(0).contains("Error retrieving users"));
    }

    @Test
    void updateUser_ShouldUpdateExistingUser() {
        
        UserDto userDto = new UserDto();
        userDto.setUsername("updateduser");
        userDto.setEmail("updated@example.com");
        userDto.setRoles(Collections.singleton("ROLE_USER"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(testRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        
        ServiceResult<UserResponseDto> result = userService.updateUser(1L, userDto);

        
        assertNotNull(result.getData());
        assertEquals("updateduser", result.getData().getUsername());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_ShouldReturnErrorWhenUserNotFound() {
        
        UserDto userDto = new UserDto();
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        
        ServiceResult<UserResponseDto> result = userService.updateUser(1L, userDto);

        
        assertTrue(result.hasErrors());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().get(0).contains("User not found"));
    }

    @Test
    void deleteUser_ShouldDeleteExistingUser() {
        
        when(userRepository.existsById(1L)).thenReturn(true);

        
        ServiceResult<String> result = userService.deleteUser(1L);

        
        assertNotNull(result.getData());
        assertEquals("User deleted successfully.", result.getData());
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteUser_ShouldReturnErrorWhenUserNotFound() {
        
        when(userRepository.existsById(1L)).thenReturn(false);

        
        ServiceResult<String> result = userService.deleteUser(1L);

        
        assertTrue(result.hasErrors());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().get(0).contains("User not found"));
    }

    @Test
    void createUser_ShouldRegisterNewUser() throws RoleNotFoundException {
        
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(testRole));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        
        User result = userService.createUser(registerRequest);

        
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_ShouldThrowWhenUsernameExists() {
        
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        
        assertThrows(UserAlreadyExistsException.class, () -> {
            userService.createUser(registerRequest);
        });
    }

    @Test
    void createUser_ShouldThrowWhenEmailExists() {
        
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        
        assertThrows(UserAlreadyExistsException.class, () -> {
            userService.createUser(registerRequest);
        });
    }

    @Test
    void registerUser_ShouldCreateUserWithDefaultRole() throws RoleNotFoundException {
        
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(testRole));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        
        User result = userService.registerUser(registerRequest);

        
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("encodedPassword", result.getPassword());
        assertEquals(1, result.getRoles().size());
        assertTrue(result.isEnabled());
        assertFalse(result.isLocked());
    }

    @Test
    void registerUser_ShouldThrowWhenRoleNotFound() {
        
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());

        
        assertThrows(RuntimeException.class, () -> {
            userService.registerUser(registerRequest);
        });
    }

    @Test
    void toDto_ShouldConvertUserToDto() {
        
        User user = testUser;

        
        UserResponseDto result = userService.toDto(user);

        
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals(1, result.getRoles().size());
        assertTrue(result.getRoles().contains("ROLE_USER"));
    }
}