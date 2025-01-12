package com.example.importservice.repository.auth;

import com.example.importservice.domain.entity.auth.User;
import com.example.importservice.domain.entity.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    List<User> findByWishToBeAdmin(boolean requestAdmin);
    boolean existsByRole_Name(RoleName name);
}
