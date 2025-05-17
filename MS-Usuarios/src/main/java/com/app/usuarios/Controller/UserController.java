package com.app.usuarios.Controller;


import com.app.usuarios.Dto.ServiceResult;
import com.app.usuarios.Dto.UserDto;
import com.app.usuarios.Dto.UserResponseDto;
import com.app.usuarios.Model.User;
import com.app.usuarios.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers() {
        ServiceResult<List<UserResponseDto>> result = userService.getAllUsers();
        return result.hasErrors()
                ? ResponseEntity.status(500).body(result.getErrors())
                : ResponseEntity.ok(result.getData());
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserDto userDto) {
        ServiceResult<UserResponseDto> result = userService.updateUser(id, userDto);
        return result.hasErrors()
                ? ResponseEntity.badRequest().body(result.getErrors())
                : ResponseEntity.ok(result.getData());
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        ServiceResult<String> result = userService.deleteUser(id);
        return result.hasErrors()
                ? ResponseEntity.status(404).body(result.getErrors())
                : ResponseEntity.ok(result.getData());
    }
}