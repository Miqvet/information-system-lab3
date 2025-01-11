package com.example.lab1.repository.auth;


import com.example.lab1.domain.entity.auth.Role;
import com.example.lab1.domain.entity.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}
