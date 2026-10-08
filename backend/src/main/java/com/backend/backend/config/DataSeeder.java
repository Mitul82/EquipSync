package com.backend.backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.backend.backend.enums.ERoles;
import com.backend.backend.models.RoleModel;
import com.backend.backend.repository.RolesRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
    private final RolesRepository repo;

    @Override
    @Transactional
    public void run(String... args) {
        for (ERoles role : ERoles.values()) {
            if (!repo.existsByRole(role)) {
                repo.save(RoleModel.builder().role(role).build());
            }
        }
    }
}