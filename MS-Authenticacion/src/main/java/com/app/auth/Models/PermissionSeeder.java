package com.app.auth.Models;

import com.app.auth.Repository.PermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class PermissionSeeder implements CommandLineRunner {

    @Autowired
    private PermissionRepository permissionRepository;

    @Override
    public void run(String... args) {
        List<String> permissions = Arrays.asList("READ_PERMISSIONS", "ADMIN_PERMISSIONS", "WRITE_PERMISSIONS");

        for (String permission : permissions) {
            if (!permissionRepository.existsByName(permission)) {
                Permission permit = new Permission();
                permit.setName(permission);
                permissionRepository.save(permit);
                System.out.println("Permiso insertado: " + permission);
            }
        }
    }
}
