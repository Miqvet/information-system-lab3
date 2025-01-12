package com.example.lab1.controller;

import com.example.lab1.domain.dto.ImportHistoryUpdate;
import com.example.lab1.service.ImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/import-history")
@RequiredArgsConstructor
public class ImportHistoryController {
    private final ImportService importService;

    @PostMapping("/update")
    public ResponseEntity<Void> updateImportStatus(@RequestBody ImportHistoryUpdate update) {
        System.out.println("\n=== [MAIN-SERVICE] Получен запрос на обновление статуса импорта ===");
        System.out.println("=== [MAIN-SERVICE] ID импорта: " + update.getImportHistoryId());
        System.out.println("=== [MAIN-SERVICE] Статус: " + update.isStatus());
        System.out.println("=== [MAIN-SERVICE] Количество элементов: " + update.getCountElement());
        
        try {
            importService.updateImportStatus(update);
            System.out.println("=== [MAIN-SERVICE] Статус успешно обновлен ===\n");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.out.println("=== [MAIN-SERVICE] Ошибка при обновлении статуса: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}