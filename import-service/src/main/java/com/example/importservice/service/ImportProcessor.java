package com.example.importservice.service;

import com.example.importservice.dto.ImportHistoryUpdate;
import com.example.importservice.dto.ImportMessage;
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
    private final ImportHistoryService importHistoryService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "${rabbitmq.queue-name}")
    public void processImport(ImportMessage message) {
        try {
            log.info("Начало обработки файла: {}", message.getFileName());
            
            InputStream fileData = minioService.downloadFile(message.getFileName());
            List<StudyGroup> studyGroups = parseStudyGroups(fileData);
            
            long savedCount = 0;
            List<String> errors = new ArrayList<>();
            
            for (StudyGroup group : studyGroups) {
                try {
                    validateStudyGroup(group);
                    studyGroupService.save(group);
                    savedCount++;
                } catch (Exception e) {
                    errors.add(String.format("Ошибка в группе %s: %s", 
                        group.getName(), e.getMessage()));
                }
            }

            ImportHistoryUpdate update = new ImportHistoryUpdate(
                message.getImportHistoryId(),
                savedCount > 0,
                "COMPLETED",
                savedCount,
                errors.isEmpty() ? null : String.join("\n", errors)
            );
            importHistoryService.updateImportStatus(update);

        } catch (Exception e) {
            log.error("Ошибка обработки импорта: ", e);
            ImportHistoryUpdate update = new ImportHistoryUpdate(
                message.getImportHistoryId(),
                false,
                "FAILED",
                0,
                e.getMessage()
            );
            importHistoryService.updateImportStatus(update);
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
}