package com.example.lab1.entity;

import com.example.lab1.entity.auth.Role;
import com.example.lab1.entity.enums.RoleName;
import com.example.lab1.repository.auth.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public DataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (roleRepository.findByName(RoleName.valueOf("ROLE_USER")).isEmpty()) {
            Role userRole = new Role();
            userRole.setName(RoleName.valueOf("ROLE_USER"));
            roleRepository.save(userRole);
        }

        if (roleRepository.findByName(RoleName.valueOf("ROLE_ADMIN")).isEmpty()) {
            Role adminRole = new Role();
            adminRole.setName(RoleName.valueOf("ROLE_ADMIN"));
            roleRepository.save(adminRole);
        }
    }
}
