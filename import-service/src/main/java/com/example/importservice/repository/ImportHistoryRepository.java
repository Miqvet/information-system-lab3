package com.example.importservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.importservice.domain.entity.ImportHistory;
import com.example.importservice.domain.entity.auth.User;

public interface ImportHistoryRepository extends JpaRepository<ImportHistory, Long> {
    List<ImportHistory> findAllByAddedBy(User user);
}
