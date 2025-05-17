package com.app.usuarios.Controller;


import com.app.usuarios.Dto.PermissionDto;
import com.app.usuarios.Dto.ServiceResult;
import com.app.usuarios.Service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping("/create")
    public ResponseEntity<?> createPermission(@RequestBody PermissionDto dto) {
        ServiceResult<?> result = permissionService.create(dto);
        return result.hasErrors()
                ? ResponseEntity.badRequest().body(result.getErrors())
                : ResponseEntity.ok(result.getData());
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updatePermission(@PathVariable Long id, @RequestBody PermissionDto dto) {
        ServiceResult<?> result = permissionService.update(id, dto);
        return result.hasErrors()
                ? ResponseEntity.badRequest().body(result.getErrors())
                : ResponseEntity.ok(result.getData());
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deletePermission(@PathVariable Long id) {
        ServiceResult<?> result = permissionService.delete(id);
        return result.hasErrors()
                ? ResponseEntity.status(404).body(result.getErrors())
                : ResponseEntity.ok(result.getData());
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllPermissions() {
        ServiceResult<?> result = permissionService.getAll();
        return result.hasErrors()
                ? ResponseEntity.status(500).body(result.getErrors())
                : ResponseEntity.ok(result.getData());
    }
}
