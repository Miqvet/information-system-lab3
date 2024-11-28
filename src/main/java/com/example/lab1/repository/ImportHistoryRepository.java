package com.example.lab1.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.lab1.domain.entity.ImportHistory;
import com.example.lab1.domain.entity.auth.User;

public interface ImportHistoryRepository extends JpaRepository<ImportHistory, Long> {
    List<ImportHistory> findAllByAddedBy(User user);
}
