package com.app.usuarios.Service;
import com.app.usuarios.Controller.UserAlreadyExistsException;
import com.app.usuarios.Dto.*;
import com.app.usuarios.Model.Role;
import com.app.usuarios.Model.*;
import com.app.usuarios.Repository.RoleRepository;
import com.app.usuarios.Repository.UserRepository;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.hibernate.service.spi.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.management.relation.RoleNotFoundException;
import java.util.*;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);


    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;


    public ServiceResult<List<UserResponseDto>> getAllUsers() {
        try {
            List<UserResponseDto> users = userRepository.findAll().stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());

            return new ServiceResult<>(users);
        } catch (Exception e) {
            return new ServiceResult<>(List.of("Error retrieving users: " + e.getMessage()));
        }
    }

    public ServiceResult<UserResponseDto> updateUser(Long id, UserDto userDto) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

            user.setUsername(userDto.getUsername());
            user.setEmail(userDto.getEmail());


            Set<Role> roles = userDto.getRoles().stream()
                    .map(name -> roleRepository.findByName(name)
                            .orElseThrow(() -> new RuntimeException("Role not found: " + name)))
                    .collect(Collectors.toSet());

            user.setRoles(roles);

            User updated = userRepository.save(user);
            return new ServiceResult<>(toDto(updated));
        } catch (Exception e) {
            return new ServiceResult<>(List.of("Error updating user: " + e.getMessage()));
        }
    }

    public ServiceResult<String> deleteUser(Long id) {
        try {
            if (!userRepository.existsById(id)) {
                return new ServiceResult<>(List.of("User not found with ID: " + id));
            }

            userRepository.deleteById(id);
            return new ServiceResult<>("User deleted successfully.");
        } catch (Exception e) {
            return new ServiceResult<>(List.of("Error deleting user: " + e.getMessage()));
        }
    }

    private UserResponseDto toDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .build();
    }

    public User createUser(RegisterRequest request) {
        Objects.requireNonNull(request, "RegisterRequest cannot be null");
        validateUserDoesNotExist(request.getUsername(), request.getEmail());
        try {
            User savedUser = registerAndSaveUser(request);
            return savedUser;

        } catch (JwtException e) {
            logger.error("JWT generation failed for user: {}", request.getUsername(), e);
            throw new ServiceException("Registration failed: could not generate access token", e);
        } catch (DataAccessException e) {
            logger.error("Database error during user registration for: {}", request.getUsername(), e);
            throw new ServiceException("Registration failed: database error", e);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during user registration", e);
            throw new ServiceException("Registration failed due to unexpected error", e);
        }
    }
    private void validateUserDoesNotExist(String username, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new UserAlreadyExistsException("Username already exists: " + username);
        }

        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("Email already exists: " + email);
        }
    }
    private User registerAndSaveUser(RegisterRequest request) {
        User user = registerUser(request);
        return userRepository.save(user);
    }
    public User registerUser(RegisterRequest userRequest) {
        // Validación de entrada
        if (userRequest == null) {
            throw new IllegalArgumentException("RegisterRequest cannot be null");
        }

        Role roleDefault = null;
        try {
            roleDefault = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RoleNotFoundException("ROLE_USER not found"));
        } catch (RoleNotFoundException e) {
            throw new RuntimeException(e);
        }

        return User.builder()
                .username(userRequest.getUsername())
                .email(userRequest.getEmail())
                .password(passwordEncoder.encode(userRequest.getPassword()))
                .roles(Collections.singleton(roleDefault))
                .locked(false)
                .enabled(true)
                .build();
    }
}
