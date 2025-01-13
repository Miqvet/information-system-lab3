package com.example.importservice.service;

import com.example.importservice.domain.dto.ImportMessage;
import com.example.importservice.domain.entity.ImportHistory;
import com.example.importservice.domain.entity.StudyGroup;
import com.example.importservice.domain.entity.auth.User;
import com.example.importservice.repository.ImportHistoryRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImportProcessor {
    private final MinioService minioService;
    private final StudyGroupService studyGroupService;
    private final UserService userService;
    private final ImportHistoryRepository importHistoryRepository;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "${rabbitmq.queue-name}")
    public void processImport(ImportMessage message) {
        System.out.println("\n=== [IMPORT-SERVICE] Получено новое сообщение для обработки ===");
        System.out.println("=== [IMPORT-SERVICE] Файл: " + message.getFileName());
        System.out.println("=== [IMPORT-SERVICE] ID импорта: " + message.getImportHistoryId());
        System.out.println("=== [IMPORT-SERVICE] ID пользователя: " + message.getUserId());
        
        try {
            User user = userService.getUserById(message.getUserId());
            System.out.println("=== [IMPORT-SERVICE] Пользователь найден: " + user.getUsername());

            ImportHistory importHistory = importHistoryRepository.findById(message.getImportHistoryId())
                    .orElseThrow(() -> new RuntimeException("История импорта не найдена"));
            
            InputStream fileData = minioService.downloadFile(message.getFileName());
            List<StudyGroup> studyGroups = parseStudyGroups(fileData);
            System.out.println("=== [IMPORT-SERVICE] Прочитано групп из файла: " + studyGroups.size());
            
            long savedCount = 0;
            List<String> errors = new ArrayList<>();
            
            for (StudyGroup group : studyGroups) {
                try {
                    validateStudyGroup(group);
                    if (group.getGroupAdmin() != null) {
                        group.getGroupAdmin().setCreatedBy(user);
                        System.out.println("=== [IMPORT-SERVICE] Установлен created_by для администратора группы: " + user.getUsername());
                    }
                    group.setCreatedBy(user);
                    System.out.println("=== [IMPORT-SERVICE] Установлен created_by для группы: " + user.getUsername());
                    
                    studyGroupService.save(group);
                    savedCount++;
                    System.out.println("=== [IMPORT-SERVICE] Сохранена группа: " + group.getName());
                } catch (Exception e) {
                    String error = String.format("Ошибка в группе %s: %s", group.getName(), e.getMessage());
                    System.out.println("=== [IMPORT-SERVICE] ОШИБКА: " + error);
                    errors.add(error);
                }
            }
            importHistory.setStatus(true);
            importHistory.setCountElement(savedCount);
            importHistory.setAddedBy(user);
            
            importHistoryRepository.save(importHistory);
            System.out.println("=== [IMPORT-SERVICE] Статус импорта обновлен в БД ===\n");

        } catch (Exception e) {
            System.out.println("=== [IMPORT-SERVICE] КРИТИЧЕСКАЯ ОШИБКА: " + e.getMessage());
            try {
                ImportHistory importHistory = importHistoryRepository.findById(message.getImportHistoryId())
                        .orElseThrow(() -> new RuntimeException("История импорта не найдена"));
                
                importHistory.setStatus(false);
                importHistory.setCountElement(0);
                importHistoryRepository.save(importHistory);
                
                System.out.println("=== [IMPORT-SERVICE] Статус ошибки сохранен в БД ===");
            } catch (Exception ex) {
                System.out.println("=== [IMPORT-SERVICE] ОШИБКА при сохранении статуса ошибки: " + ex.getMessage());
            }
        }
    }

    private List<StudyGroup> parseStudyGroups(InputStream inputStream) {
        try {
            return objectMapper.readValue(inputStream,
                    new TypeReference<List<StudyGroup>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Ошибка парсинга файла: " + e.getMessage(), e);
        }
    }

    private void validateStudyGroup(StudyGroup group) {
        List<String> errors = new ArrayList<>();

        if (group.getName() == null || group.getName().trim().isEmpty()) {
            errors.add("Название группы не может быть пустым");
        }

        if (group.getCoordinates() == null) {
            errors.add("Coordinates не может быть null");
        }

        if (group.getStudentsCount() == null || group.getStudentsCount() < 0) {
            errors.add("Количество студентов должно быть положительным числом");
        }

        if (!errors.isEmpty()) {
            throw new RuntimeException(String.join(", ", errors));
        }
    }
}