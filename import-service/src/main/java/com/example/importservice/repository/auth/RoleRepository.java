package com.example.importservice.repository.auth;


import com.example.importservice.domain.entity.auth.Role;
import com.example.importservice.domain.entity.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}
