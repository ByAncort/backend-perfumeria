package com.app.usuarios.Service;
import com.app.usuarios.Dto.UserDto;
import com.app.usuarios.Dto.UserResponseDto;
import com.app.usuarios.Model.Role;
import com.app.usuarios.Model.*;
import com.app.usuarios.Repository.RoleRepository;
import com.app.usuarios.Dto.ServiceResult;
import com.app.usuarios.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class UserService {

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
}
