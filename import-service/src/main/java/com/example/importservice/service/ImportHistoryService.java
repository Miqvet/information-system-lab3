package com.example.importservice.service;

import com.example.importservice.domain.dto.ImportHistoryUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImportHistoryService {
    private final RestTemplate restTemplate;

    @Value("${main-service.url}")
    private String mainServiceUrl;

    public void updateImportStatus(ImportHistoryUpdate update) {
        String url = mainServiceUrl + "/api/import-history/update";
        try {
            System.out.println("=== [IMPORT-SERVICE] Отправка обновления статуса в main-service ===");
            System.out.println("=== [IMPORT-SERVICE] URL: " + url);
            System.out.println("=== [IMPORT-SERVICE] Данные: " + update);
            
            ResponseEntity<Void> response = restTemplate.postForEntity(url, update, Void.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("=== [IMPORT-SERVICE] Статус успешно обновлен ===");
            } else {
                System.out.println("=== [IMPORT-SERVICE] Ошибка обновления статуса: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.out.println("=== [IMPORT-SERVICE] Ошибка при обновлении статуса: " + e.getMessage());
            throw new RuntimeException("Не удалось обновить статус импорта", e);
        }
    }
}