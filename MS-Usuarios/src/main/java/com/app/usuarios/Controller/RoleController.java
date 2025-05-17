package com.app.usuarios.Controller;

import com.app.usuarios.Dto.*;
import com.app.usuarios.Model.Role;
import com.app.usuarios.Service.RoleService;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/roles")
public class RoleController {
    private final RoleService roleService;

    @PostMapping("create")
    public ResponseEntity<?> createRole(@RequestBody RoleDto roleDto) {
        ServiceResult<Role> result = roleService.create(roleDto);

        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result.getErrors());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(result.getData());
    }

    @DeleteMapping("delete-role/{id}")
    public ResponseEntity<?> deleteRole(@PathVariable Long id) {
        ServiceResult<String> result = roleService.deleteById(id);

        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result.getErrors());
        }

        return ResponseEntity.ok(result.getData());
    }

    @PutMapping("/{RoleName}/assign-permission/{permissionName}")
    public ResponseEntity<?> assignPermission(@PathVariable String RoleName, @PathVariable String permissionName) {
        ServiceResult<Role> result = roleService.assignPermissionToRole(RoleName, permissionName);

        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result.getErrors());
        }

        return ResponseEntity.ok(result.getData());
    }

    @PutMapping("/{RoleName}/remove-permission/{permissionName}")
    public ResponseEntity<?> removePermission(@PathVariable String RoleName, @PathVariable String permissionName) {
        ServiceResult<Role> result = roleService.removePermissionFromRole(RoleName, permissionName);

        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result.getErrors());
        }

        return ResponseEntity.ok(result.getData());
    }
    @GetMapping("/all")
    public ResponseEntity<?> getAllRoles() {
        ServiceResult<?> result = roleService.getAllRoles();

        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result.getErrors());
        }

        return ResponseEntity.ok(result.getData());
    }

}
